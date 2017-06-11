package com.vishalzanzrukia.crawler.context;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Sets;
import com.vishalzanzrukia.crawler.parser.ProductParser;
import com.vishalzanzrukia.crawler.registry.ComponentRegistry;
import com.vishalzanzrukia.crawler.urlprocessor.UrlProcessor;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class ConfigContext {

	@Value("${redis.host}")
	private String redisHost;

	@Value("${redis.port}")
	private int redisPort;

	@Value("#{'${crawler.supported.domains}'.split(',')}")
	private Set<String> supportedDomains;

	@Bean
	public JedisPoolConfig jedisPoolConfig() {
		return new JedisPoolConfig();
	}

	@Bean
	public JedisPool getJedisPool(final JedisPoolConfig jedisPoolConfig) {
		return new JedisPool(jedisPoolConfig, redisHost, redisPort);
	}

	@Bean
	public ComponentRegistry componentRegistry() {
		return new ComponentRegistry(supportedDomains, Sets.newHashSet(ProductParser.ID, UrlProcessor.ID));
	}
}
