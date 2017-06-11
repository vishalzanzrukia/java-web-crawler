package com.vishalzanzrukia.crawler;

import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.Connection;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.support.JmsUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import com.vishalzanzrukia.crawler.integration.CustomMessageListenerContainer;
import com.vishalzanzrukia.crawler.registry.SingletonBeanFactory;
import com.vishalzanzrukia.crawler.util.CrawlerUtils.MESSAGE_HEADERS;

import redis.clients.jedis.Jedis;

/**
 * This class is responsible to trigger the process, it will check whether any
 * crawler was running while last JVM shutdown, if yes it will start from last
 * point, or else trigger the crawler process from zero.
 * 
 * @author VishalZanzrukia
 */
@Component
public class ProcessTriggeringBean extends AbstractLifecycleAdapter implements ApplicationContextAware {

	private static final Logger LOG = LogManager.getLogger();

	@Autowired
	private SingletonBeanFactory singletonBeanFactory;

	@Autowired
	@Qualifier("visitUrlChannelProducer")
	private MessageChannel triggerChannel;

	private ApplicationContext applicationContext;

	/**
	 * This will help to decide whether crawler needs to trigger after context
	 * startup or not.
	 */
	private AtomicBoolean isFirstTime = new AtomicBoolean();

	/**
	 * This will save the current time in millis whenever crawler triggers the
	 * process.
	 */
	private AtomicLong lastTimeTriggered = new AtomicLong();

	/**
	 * set that process has been triggered once after startup.
	 */
	public void setProcessTriggeredOnce() {
		LOG.trace("Setting the isFirstTime flag to false");
		isFirstTime.compareAndSet(true, false);
	}

	public boolean isQueueEmpty(final String queueName) {
		Connection connection = null;
		Session session = null;
		QueueBrowser queueBrowser = null;
		try {

			/** TODO : We can use singletonBeanFactory for getting beans */
			CachingConnectionFactory connectionFactory = (CachingConnectionFactory) applicationContext.getBean("jms.url.cachingConnectionFactory");
			connection = connectionFactory.createConnection();

			/** TODO : We can use singletonBeanFactory for getting beans */
			ActiveMQQueue queue = (ActiveMQQueue) applicationContext.getBean(queueName);
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			queueBrowser = session.createBrowser(queue);

			@SuppressWarnings("rawtypes")
			Enumeration enu = queueBrowser.getEnumeration();
			return !enu.hasMoreElements();
		} catch (Exception e) {
			LOG.error("Error while retrieving the visit queue status for pending messages", e);
			throw new RuntimeException(e);
		} finally {
			JmsUtils.closeQueueBrowser(queueBrowser);
			JmsUtils.closeSession(session);
			JmsUtils.closeConnection(connection, true);
			LOG.trace("Connections (opened for checking running process) has been closed successfully.");
		}
	}

	/**
	 * This will check that both <code>productUrlQ</code> and
	 * <code>visitUrlQ</code> queues whether those are processed or not<BR>
	 * If yes, this will restart the crawler process.
	 *
	 * @return true, if crawler has been restarted
	 */
	public synchronized void checkForRestartProcess() {

		final CustomMessageListenerContainer visitUrlListenerContainer = (CustomMessageListenerContainer) applicationContext
				.getBean("visitUrlMessageListenerContainer");
		final CustomMessageListenerContainer productUrlListenerContainer = (CustomMessageListenerContainer) applicationContext
				.getBean("productUrlMessageListenerContainer");

		if (visitUrlListenerContainer.isQueueEmpty().get()) {
			LOG.debug("The visitUrlQ is empty!");

			if (productUrlListenerContainer.isQueueEmpty().get()) {
				LOG.info("The visitUrlQ as well the productUrlQ are empty, Going to try to restart the crawler now.");

				triggerCrawlerProcess(visitUrlListenerContainer.getLastTimeReceived(), productUrlListenerContainer.getLastTimeReceived());
			}
		}
	}

	@Override
	public void start() {
		LOG.debug("Going to start ProcessTriggeringBean, setting the isFirstTime flag to true!");
		isFirstTime.compareAndSet(false, true);

		final String robotsTxtUrl = singletonBeanFactory.getRuntimeConfigs().getRobotsTxtUrl();
		if (robotsTxtUrl == null) {
			LOG.warn("Not able to retrieve robots.txt file for domain : {}", singletonBeanFactory.getRuntimeConfigs().getDomainName());
		} else {
			LOG.debug("Extracted robotTxtUrl : {} from domain name : {}", robotsTxtUrl, singletonBeanFactory.getRuntimeConfigs().getDomainName());
			singletonBeanFactory.getRobotsTxtParser().parse(robotsTxtUrl);
		}

		/**
		 * setting this to past time to ignore
		 * getMinimumIntervalBetweenTwoTriggers for first time
		 */
		lastTimeTriggered.set(System.currentTimeMillis() - (singletonBeanFactory.getConfigs().getMinimumIntervalBetweenTwoTriggers() * 60 * 1000));
	}

	@Override
	public int getPhase() {
		/** It will call start at last and stop at first! */
		return Integer.MAX_VALUE;
	}

	@Override
	public void stop() {
		LOG.info("Inside ProcessTriggeringBean.stop, destoying redis client");
		singletonBeanFactory.getJedisPool().destroy();
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * Helper method to created jms message for triggering the crawler process.
	 */
	private Message<String> createMessage() {
		/** persist init URL in visited urls store */
		try (Jedis client = getRedisClient()) {
			client.sadd(singletonBeanFactory.getRuntimeConfigs().getVisitedUrlRedisKey(),
					singletonBeanFactory.getCrawlerUtils().trimHttps(singletonBeanFactory.getConfigs().getTriggerUrl()));
		}

		return MessageBuilder.withPayload(StringUtils.EMPTY).setHeader(MESSAGE_HEADERS.URL, singletonBeanFactory.getConfigs().getTriggerUrl())
				.setHeader(MESSAGE_HEADERS.DEPTH, 0).build();
	}

	/**
	 * Helper method to delete all Redis database stores.
	 */
	private void deleteDB() {
		try (Jedis client = getRedisClient()) {
			client.del(singletonBeanFactory.getRuntimeConfigs().getVisitedUrlRedisKey());
		}
		try (Jedis client = getRedisClient()) {
			client.del(singletonBeanFactory.getRuntimeConfigs().getParsedProductsRedisKey());
		}
	}

	/**
	 * Trigger crawler process.
	 */
	private synchronized void triggerCrawlerProcess(final AtomicLong visitQLasttimeReceived, final AtomicLong productQLasttimeReceived) {
		if (isFirstTime.get()) {
			LOG.debug("There are no pending messages in queues, going to trigger crawler process after startup.");

		} else {
			if (singletonBeanFactory.getCrawlerUtils().diffInMins(visitQLasttimeReceived.get()) >= singletonBeanFactory.getConfigs()
					.getCrawlerDuration()
					&& singletonBeanFactory.getCrawlerUtils().diffInMins(productQLasttimeReceived.get()) >= singletonBeanFactory.getConfigs()
							.getCrawlerDuration()) {

				if (LOG.isDebugEnabled()) {

					long minDifference = singletonBeanFactory.getCrawlerUtils().diffInMins(visitQLasttimeReceived.get());
					LOG.debug("No progress done on visitUrlQ since last {} minutes.", minDifference);

					minDifference = singletonBeanFactory.getCrawlerUtils().diffInMins(productQLasttimeReceived.get());
					LOG.debug("No progress done on productUrlQ since last {} minutes.", minDifference);
				}

			} else {

				if (LOG.isDebugEnabled()) {

					final long visitQDurationAfterLastProgress = singletonBeanFactory.getCrawlerUtils().diffInMins(visitQLasttimeReceived.get());
					final long productQDurationAfterLastProgress = singletonBeanFactory.getCrawlerUtils().diffInMins(productQLasttimeReceived.get());
					final long idleDurationAfterLastProgress;

					if (productQDurationAfterLastProgress > visitQDurationAfterLastProgress) {
						idleDurationAfterLastProgress = visitQDurationAfterLastProgress;
					} else {
						idleDurationAfterLastProgress = productQDurationAfterLastProgress;
					}

					LOG.info("Crawler will wait for {} mins approx before starting new cycle.",
							(singletonBeanFactory.getConfigs().getCrawlerDuration() - idleDurationAfterLastProgress));
				}

				return;
			}
		}

		if (singletonBeanFactory.getCrawlerUtils().diffInMins(lastTimeTriggered.get()) >= singletonBeanFactory.getConfigs()
				.getMinimumIntervalBetweenTwoTriggers()) {

			if (isQueueEmpty("productUrlQ") && isQueueEmpty("visitUrlQ")) {

				LOG.info("Setting the last triggered time to current time.");
				lastTimeTriggered.set(System.currentTimeMillis());

				deleteDB();
				LOG.info("DB deleted successfully.");

				triggerChannel.send(createMessage());
				LOG.info("Crwaler process triggered successfully.!!!");

			} else {

				LOG.warn(
						"Crawler is not getting any messages while reading using MessageConsumer but queues are still not empty!, so skipping to restart the crawler");
			}

		} else {
			LOG.info("Skipping to trigger crawler process twice within {} mins",
					singletonBeanFactory.getConfigs().getMinimumIntervalBetweenTwoTriggers());
		}
	}

	public Jedis getRedisClient() {
		return singletonBeanFactory.getJedisPool().getResource();
	}
}
