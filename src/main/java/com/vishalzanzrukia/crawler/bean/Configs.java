package com.vishalzanzrukia.crawler.bean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class will load all static configs from property file.
 * 
 * @author VishalZanzrukia
 */
@Component
public class Configs {

	/** Max depth to visit start from seed page, maximum recommendation is 5 */
	@Value("${crawler.url.maxDepth}")
	private int maxDepth;

	/** Max number of urls to visit per second */
	@Value("${crawler.url.maxVisit}")
	private double maxVisit;

	/** Timeout in seconds for retrieving single page */
	@Value("${crawler.page.timeout}")
	private int timeout;

	/**
	 * Max number of retry in case desired data not received while
	 * fetching/parsing
	 */
	@Value("${crawler.page.maxRetry}")
	private int maxRetry;

	/** Max bytes per page to crawling */
	@Value("${crawler.page.maxBytes}")
	private int maxBytes;

	/** Start url to trigger the process */
	@Value("${crawler.triggerUrl}")
	private String triggerUrl;

	/** The time gap in minutes between two crawler cycles */
	@Value("${crawler.cycle.period}")
	private int crawlerDuration;

	/**
	 * The minimum minutes interval between two crawler process triggering
	 * events.
	 */
	private int minimumIntervalBetweenTwoTriggers = 1;

	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public double getMaxVisit() {
		return maxVisit;
	}

	public void setMaxVisit(double maxVisit) {
		this.maxVisit = maxVisit;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getMaxRetry() {
		return maxRetry;
	}

	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}

	public int getMaxBytes() {
		return maxBytes;
	}

	public void setMaxBytes(int maxBytes) {
		this.maxBytes = maxBytes;
	}

	public String getTriggerUrl() {
		return triggerUrl;
	}

	public void setTriggerUrl(String triggerUrl) {
		this.triggerUrl = triggerUrl;
	}

	public int getCrawlerDuration() {
		return crawlerDuration;
	}

	public void setCrawlerDuration(int crawlerDuration) {
		this.crawlerDuration = crawlerDuration;
	}

	public int getMinimumIntervalBetweenTwoTriggers() {
		return minimumIntervalBetweenTwoTriggers;
	}

	public void setMinimumIntervalBetweenTwoTriggers(int minimumIntervalBetweenTwoTriggers) {
		this.minimumIntervalBetweenTwoTriggers = minimumIntervalBetweenTwoTriggers;
	}
}
