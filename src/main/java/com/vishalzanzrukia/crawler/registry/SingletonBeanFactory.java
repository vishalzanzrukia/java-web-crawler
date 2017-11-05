package com.vishalzanzrukia.crawler.registry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.RateLimiter;
import com.vishalzanzrukia.crawler.ProcessTriggeringBean;
import com.vishalzanzrukia.crawler.bean.Configs;
import com.vishalzanzrukia.crawler.bean.RuntimeConfigs;
import com.vishalzanzrukia.crawler.parser.RobotsTxtParser;
import com.vishalzanzrukia.crawler.util.ContentDownloader;
import com.vishalzanzrukia.crawler.util.ContentProvider;
import com.vishalzanzrukia.crawler.util.CrawlerUtils;

import crawlercommons.filters.basic.BasicURLNormalizer;
import crawlercommons.robots.SimpleRobotRulesParser;
import crawlercommons.sitemaps.SiteMapParser;
import redis.clients.jedis.JedisPool;

/**
 * The bean registry for all singleton classes used within crawler.
 * 
 * @author VishalZanzrukia
 */
@Component
public class SingletonBeanFactory {

	private static final Logger LOG = LogManager.getLogger();

	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * Gets the bean.
	 *
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the type
	 * @return the bean
	 */
	public <T> T getBean(Class<T> type) {
		T instance = null;
		try {
			instance = applicationContext.getBean(type);
		} catch (NoUniqueBeanDefinitionException e) {
			LOG.error("There are more than one objects found for class : {}", type, e);
		} catch (NoSuchBeanDefinitionException e) {
			LOG.error("There is no object created for for class : {}", type, e);
			throw e;
		}

		return instance;
	}

	public Configs getConfigs() {
		return getBean(Configs.class);
	}

	public RuntimeConfigs getRuntimeConfigs() {
		return getBean(RuntimeConfigs.class);
	}

	public RateLimiter getRateLimiter() {
		return getBean(RateLimiter.class);
	}

	public CrawlerUtils getCrawlerUtils() {
		return getBean(CrawlerUtils.class);
	}

	public RobotsTxtParser getRobotsTxtParser() {
		return getBean(RobotsTxtParser.class);
	}

	public ComponentRegistry getComponentRegistry() {
		return getBean(ComponentRegistry.class);
	}

	public JedisPool getJedisPool() {
		return getBean(JedisPool.class);
	}

	public SiteMapParser getSiteMapParser() {
		return getBean(SiteMapParser.class);
	}

	public SimpleRobotRulesParser getSimpleRobotRulesParser() {
		return getBean(SimpleRobotRulesParser.class);
	}

	public ContentProvider getContentProvider() {
		return getBean(ContentDownloader.class);
	}

	public ProcessTriggeringBean getProcessTriggeringBean() {
		return getBean(ProcessTriggeringBean.class);
	}

	public BasicURLNormalizer getBasicURLNormalizer() {
		return getBean(BasicURLNormalizer.class);
	}
}
