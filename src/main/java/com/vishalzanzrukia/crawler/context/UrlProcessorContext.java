package com.vishalzanzrukia.crawler.context;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vishalzanzrukia.crawler.urlprocessor.UrlProcessorAdapter;

import crawlercommons.filters.basic.BasicURLNormalizer;

/**
 * The Class UrlProcessorContext.
 * 
 * @author VishalZanzrukia
 */
@Configuration
public class UrlProcessorContext {

	@Bean(initMethod = "register")
	public UrlProcessorAdapter atUrlProcessor(@Value("${crawler.supported.domain.amazon.com}") final String domainName) {
		return new UrlProcessorAdapter(domainName);
	}

	@Bean
	public BasicURLNormalizer basicURLNormalizer() {
		return new BasicURLNormalizer();
	}
}
