package com.vishalzanzrukia.crawler.parser;

import java.util.Set;

import org.apache.commons.lang3.ClassUtils;
import org.jsoup.nodes.Document;

import com.vishalzanzrukia.crawler.bean.Product;
import com.vishalzanzrukia.crawler.registry.ComponentRegistry;
import com.vishalzanzrukia.crawler.registry.CrawlerComponent;

/**
 * The interface to defines basic necessary methods to implement for all zone
 * parsers.
 * 
 * @author VishalZanzrukia
 */
public interface ProductParser extends CrawlerComponent {

	/**
	 * The id to identify parsers while retrieving it from registry
	 * {@link ComponentRegistry#get(String, String)}
	 */
	public static final String ID = ClassUtils.getShortCanonicalName(ProductParser.class);

	default String getComponentId() {
		return ID;
	}

	/**
	 * Parses the links.
	 *
	 * @param document
	 *            the document
	 * @return the sets the
	 */
	Set<String> parseUrls(final Document document);

	/**
	 * Parses the product.
	 *
	 * @param productPageUrl
	 *            the product page url
	 * @return the crawler product model
	 */
	Product parseProduct(final String productPageUrl);
}
