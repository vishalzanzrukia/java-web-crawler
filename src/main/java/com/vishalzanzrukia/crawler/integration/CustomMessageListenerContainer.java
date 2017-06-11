package com.vishalzanzrukia.crawler.integration;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.vishalzanzrukia.crawler.registry.SingletonBeanFactory;

/**
 * The customized message listener container for <code>productUrlQ</code> and
 * <code>visitUrlQ</code> queues.<BR>
 * This will manage a flag whether queue as been processed or not. Once this
 * will observe that there are no new messages arriving on particular queue,
 * then it will wait for <code>timeOutInMillis</code> minutes and then this will
 * check for restarting the crawler. Before restarting crawler the method
 * <code>checkForRestartProcess</code> will check that both queues have finished
 * processes.
 * 
 * @author VishalZanzrukia
 */
public class CustomMessageListenerContainer extends DefaultMessageListenerContainer {

	private static final Logger LOG = LogManager.getLogger();

	@Autowired
	private SingletonBeanFactory singletonBeanFactory;

	private final AtomicLong lastTimeReceived;
	private final AtomicLong lastTimeChecked;
	private AtomicBoolean isQueueEmpty = new AtomicBoolean(false);

	private final long delayBetweenTwoChecksInMins;
	private final String containerId;

	public CustomMessageListenerContainer(final String containerId, final long delayBetweenTwoChecksInMins) {
		this.containerId = containerId;
		this.delayBetweenTwoChecksInMins = delayBetweenTwoChecksInMins;

		/**
		 * setting this to past time to ignore delayBetweenTwoChecksInMins for
		 * first time
		 */
		lastTimeChecked = new AtomicLong(System.currentTimeMillis() - (delayBetweenTwoChecksInMins * 60 * 1000));
		lastTimeReceived = new AtomicLong(System.currentTimeMillis());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws JmsException {
		LOG.debug("The {} container Started", this.containerId);
		super.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void messageReceived(Object invoker, Session session) {

		LOG.trace("New message arrived, setting queueEmpty flag to false and last arrived time to current time");

		/**
		 * lastTimeReceived is set again to actual time at new message arrive
		 */
		lastTimeReceived.set(System.currentTimeMillis());

		/** set empty queue flag to false, as still message arrived */
		isQueueEmpty.compareAndSet(true, false);

		/** set that process has been triggered once after startup */
		singletonBeanFactory.getProcessTriggeringBean().setProcessTriggeredOnce();

		super.messageReceived(invoker, session);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void noMessageReceived(final Object invoker, final Session session) {

		if (LOG.isTraceEnabled()) {
			long secondDifference = singletonBeanFactory.getCrawlerUtils().diffInSeconds(lastTimeReceived.get());
			LOG.trace("[{}] : No messages arrived since last {} seconds.", this.containerId, secondDifference);
		}

		if (singletonBeanFactory.getCrawlerUtils().diffInMins(lastTimeChecked.get()) >= this.delayBetweenTwoChecksInMins) {

			if (LOG.isDebugEnabled()) {

				long minDifference = singletonBeanFactory.getCrawlerUtils().diffInMins(lastTimeReceived.get());
				LOG.trace("[{}] : No messages arrived since last {} minutes.", this.containerId, minDifference);

				LOG.debug("Not checked for restarting crawler in last {} mins, so trying to check for restart the crawler now.",
						this.delayBetweenTwoChecksInMins);
			}

			isQueueEmpty.compareAndSet(false, true);

			/**
			 * set to current time not call <code>checkForRestartProcess</code>
			 * very frequently
			 */
			lastTimeChecked.set(System.currentTimeMillis());

			singletonBeanFactory.getProcessTriggeringBean().checkForRestartProcess();
		}

		super.noMessageReceived(invoker, session);
	}

	// getter setter

	public AtomicBoolean isQueueEmpty() {
		return isQueueEmpty;
	}

	public AtomicLong getLastTimeReceived() {
		return this.lastTimeReceived;
	}
}
