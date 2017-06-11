package com.vishalzanzrukia.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.google.common.util.concurrent.RateLimiter;
import com.vishalzanzrukia.crawler.registry.SingletonBeanFactory;

@ImportResource(locations = {"classpath:jms-context.xml", "classpath:integration-context.xml"})
// @Import(value = {ApplicationContextBeans.class, ConfigContextBeans.class})
@ComponentScan(basePackages = {"com.vishalzanzrukia"})
@Configuration
@SpringBootApplication
@EnableAutoConfiguration(exclude = JmsAutoConfiguration.class)
public class Main {

	private static final Logger LOG = LogManager.getLogger();

	@Autowired
	private SingletonBeanFactory singletonBeanFactory;

	public static void main(String[] args) {
		LOG.info("Starting the application!!!!!");
		SpringApplication.run(Main.class, args);
	}

	@Bean
	public RateLimiter rateLimiter() {
		return RateLimiter.create(singletonBeanFactory.getConfigs().getMaxVisit());
	}
}
