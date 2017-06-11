/*
 * 
 */
package com.vishalzanzrukia.crawler.parser;

import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vishalzanzrukia.crawler.bean.JsoupDocumentWrapper;
import com.vishalzanzrukia.crawler.registry.SingletonBeanFactory;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRules.RobotRulesMode;
import crawlercommons.robots.SimpleRobotRulesParser;

/**
 * The class to parse the {@code robots.txt} file and extract
 * {@link BaseRobotRules}.
 * 
 * @author VishalZanzrukia
 * @see {@link BaseRobotRules}
 * @see {@link SimpleRobotRulesParser}
 */
@Component
public class RobotsTxtParser {

	private static final Logger LOG = LogManager.getLogger();

	@Autowired
	private SingletonBeanFactory singletonBeanFactory;

	private static final String CACHE_KEY = "robotsTxtRulesKey";
	/** The CACHE to store robots.txt rules */
	private static final Hashtable<String, BaseRobotRules> CACHE = new Hashtable<String, BaseRobotRules>();

	/**
	 * A {@link BaseRobotRules} object appropriate for use when the
	 * {@code robots.txt} file is empty or missing; all requests are allowed.
	 */
	private static final BaseRobotRules EMPTY_RULES = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);

	/**
	 * This will parse the robots txt rules.<BR>
	 * <b>NOTE:</b>This method must be called just once on startup.
	 *
	 * @param robotsTxtUrl
	 *            the robots txt url
	 */
	public void parse(final String robotsTxtUrl) {

		BaseRobotRules robotRules = CACHE.get(CACHE_KEY);

		if (robotRules != null) {
			LOG.trace("The robot.txt rules are already exist in cache, no needs to parse again.");
			return;
		}

		try {

			LOG.debug("Downloading the robots.txt page");
			System.err.println(singletonBeanFactory.getContentProvider());
			final JsoupDocumentWrapper response = singletonBeanFactory.getContentProvider().downloadPage(robotsTxtUrl, true);

			robotRules = singletonBeanFactory.getSimpleRobotRulesParser().parseContent(robotsTxtUrl, response.getResponseBytes(),
					response.getResponseType(), "*");

			if (robotRules != null) {
				LOG.debug("Parsed robots.txt successfully for url : {}", robotsTxtUrl);
			} else {
				LOG.warn("Not able to parsed robots.txt for url : {}", robotsTxtUrl);
			}

		} catch (Exception e) {
			LOG.error("Error while parsing robots.txt page, so skipping to follow robots.txt for url : {}", robotsTxtUrl, e);
			robotRules = EMPTY_RULES;
		}

		CACHE.put(CACHE_KEY, robotRules);
	}

	/**
	 * Checks if is allow.
	 *
	 * @param url
	 *            the url
	 * @return true, if is allow
	 */
	public boolean isAllow(final String url) {

		BaseRobotRules robotRules = CACHE.get(CACHE_KEY);
		if (robotRules == null) {
			LOG.error("Robots txt parser rules not initialized on startup, skipping to follow robots.txt rules");
			return true;
		}

		boolean isAllowed = CACHE.get(CACHE_KEY).isAllowed(url);
		LOG.trace("Url [{}] allowed against the robots.txt rules? : {}", url, isAllowed);
		return isAllowed;
	}

	/**
	 * Clean cache.
	 */
	public void cleanCache() {
		CACHE.remove(CACHE_KEY);
	}
}
