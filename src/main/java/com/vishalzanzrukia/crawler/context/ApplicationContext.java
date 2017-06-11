package com.vishalzanzrukia.crawler.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import crawlercommons.robots.SimpleRobotRulesParser;
import crawlercommons.sitemaps.SiteMapParser;

@Configuration
public class ApplicationContext {

	@Bean
	public SiteMapParser siteMapParser() {
		return new SiteMapParser(false);
	}

	@Bean
	public SimpleRobotRulesParser simpleRobotRulesParser() {
		return new SimpleRobotRulesParser();
	}

}
