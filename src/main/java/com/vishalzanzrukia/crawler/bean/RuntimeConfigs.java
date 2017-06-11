package com.vishalzanzrukia.crawler.bean;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vishalzanzrukia.crawler.registry.SingletonBeanFactory;

/**
 * All configs which are dependent on {@link Configs}<BR>
 * This all are set run time when application tries to access it first time,
 * otherwise return already set values
 * 
 * @author VishalZanzrukia
 */
@Component
public class RuntimeConfigs {

	private static final Logger LOG = LogManager.getLogger();
	private static final String VISITED_URLS_REDIS_KEY_PREFIX = "visitedUrls-";
	private static final String PARSED_PRODUCTS_REDIS_KEY_PREFIX = "parsedProductIds-";

	@Autowired
	private SingletonBeanFactory singletonBeanFactory;

	/** The domain name of the site which is being crawl, like amazon.com */
	private String domainName;

	/** The redis key to store zone specific visited URLs */
	private String visitedUrlRedisKey;

	/** The redis key to store zone specific parsed products */
	private String parsedProductsRedisKey;

	/** The robots.txt url */
	private String robotsTxtUrl;

	public String getDomainName() {
		if (domainName == null) {
			try {
				final URL url = new URL(singletonBeanFactory.getConfigs().getTriggerUrl());
				if (url == null || StringUtils.isEmpty(url.getProtocol())) {
					LOG.error("Not able to retrieve domain name from sitemap/trigger url : {}", singletonBeanFactory.getConfigs().getTriggerUrl());
					throw new IllegalArgumentException(
							"Not able to retrieve domain name from sitemap/trigger url : " + singletonBeanFactory.getConfigs().getTriggerUrl());
				}

				String domainName = null;
				if (url.getHost().startsWith("www.")) {
					domainName = url.getHost().substring(4, url.getHost().length());
				} else {
					domainName = url.getHost();
				}
				LOG.trace("The DomainName for trigger url : {}, {}", domainName, singletonBeanFactory.getConfigs().getTriggerUrl());

				setDomainName(domainName);

			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(
						"Error while retrieving protocol from sitemap/trigger url : " + singletonBeanFactory.getConfigs().getTriggerUrl(), e);
			}
		}
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getVisitedUrlRedisKey() {
		if (visitedUrlRedisKey == null) {
			setVisitedUrlRedisKey(VISITED_URLS_REDIS_KEY_PREFIX + getDomainName());
		}
		return visitedUrlRedisKey;
	}

	public void setVisitedUrlRedisKey(String visitedUrlRedisKey) {
		this.visitedUrlRedisKey = visitedUrlRedisKey;
	}

	public String getParsedProductsRedisKey() {
		if (parsedProductsRedisKey == null) {
			setParsedProductsRedisKey(PARSED_PRODUCTS_REDIS_KEY_PREFIX + getDomainName());
		}
		return parsedProductsRedisKey;
	}

	public void setParsedProductsRedisKey(String parsedProductsRedisKey) {
		this.parsedProductsRedisKey = parsedProductsRedisKey;
	}

	public String getRobotsTxtUrl() {

		if (robotsTxtUrl == null) {
			try {
				final URL url = new URL(singletonBeanFactory.getConfigs().getTriggerUrl());
				if (url == null || StringUtils.isEmpty(url.getProtocol())) {
					LOG.error("Not able to retrieve protocol from sitemap/trigger url : {}", singletonBeanFactory.getConfigs().getTriggerUrl());
					throw new IllegalArgumentException(
							"Not able to retrieve protocol from sitemap/trigger url : " + singletonBeanFactory.getConfigs().getTriggerUrl());
				}

				final String robotTxtUrl = url.getProtocol() + "://" + url.getHost() + "/robots.txt";
				LOG.trace("Robot.txt url : {}", robotTxtUrl);

				setRobotsTxtUrl(robotTxtUrl);
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(
						"Error while retrieving protocol from sitemap/trigger url : " + singletonBeanFactory.getConfigs().getTriggerUrl(), e);
			}
		}

		return robotsTxtUrl;
	}

	public void setRobotsTxtUrl(String robotsTxtUrl) {
		this.robotsTxtUrl = robotsTxtUrl;
	}
}
