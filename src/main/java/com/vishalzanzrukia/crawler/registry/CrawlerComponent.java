package com.vishalzanzrukia.crawler.registry;

import com.vishalzanzrukia.crawler.parser.ProductParser;
import com.vishalzanzrukia.crawler.urlprocessor.UrlProcessor;

/**
 * The basic component to register for different zones.
 * 
 * @author VishalZanzrukia
 * @see {@link ComponentRegistry}
 * @see {@link UrlProcessor}
 * @see {@link ProductParser}
 */
public interface CrawlerComponent {

	/**
	 * Gets the component id.
	 *
	 * @return the component id
	 */
	String getComponentId();

	/**
	 * Gets the domain name
	 *
	 * @return the domain name
	 */
	String getDomainName();

	/**
	 * Register the component
	 */
	void register();
}
