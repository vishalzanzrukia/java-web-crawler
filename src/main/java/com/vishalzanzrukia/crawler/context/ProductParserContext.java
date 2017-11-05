package com.vishalzanzrukia.crawler.context;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vishalzanzrukia.crawler.parser.AmazonComProductParser;

/**
 * The Class ProductParserContextBeans.
 * 
 * @author VishalZanzrukia
 */
@Configuration
public class ProductParserContext {

	@Bean(initMethod = "register")
	public AmazonComProductParser atProductParser(@Value("${crawler.supported.domain.propertyfinder.ae}") final String domainName) {
		return new AmazonComProductParser(domainName);
	}
}
