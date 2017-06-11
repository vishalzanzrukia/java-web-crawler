package com.vishalzanzrukia.crawler.urlprocessor;

import org.apache.commons.lang3.ClassUtils;

import com.vishalzanzrukia.crawler.registry.ComponentRegistry;
import com.vishalzanzrukia.crawler.registry.CrawlerComponent;
import com.vishalzanzrukia.crawler.util.CrawlerUtils.REGEXES;

/**
 * The interface which defines methods for url specific domain based operations
 * like {@code trim}, {@code recognize} etc. <BR>
 * We can add such features in this component in future.
 * 
 * @author VishalZanzrukia
 */
public interface UrlProcessor extends CrawlerComponent {

	/**
	 * The id to identify parsers while retrieving it from registry
	 * {@link ComponentRegistry#get(String, String)}
	 */
	public static final String ID = ClassUtils.getShortCanonicalName(UrlProcessor.class);

	/** The Constant QUERY_SEPARATOR. */
	public static final String QUERY_SEPARATOR = "?";

	/** The Constant QUERY_PARAM_SEPARATOR. */
	public static final String QUERY_PARAM_SEPARATOR = "&";

	/** The Constant PARAM_KEY_VALUE_SEPARATOR. */
	public static final String PARAM_KEY_VALUE_SEPARATOR = "=";

	default String getComponentId() {
		return ID;
	}

	/**
	 * Recognize whether given url is product url or not.
	 *
	 * @param url
	 *            the product url
	 * @return true, if given url is product url
	 */
	boolean isProductUrl(String url);

	/**
	 * Trim product url.
	 *
	 * @param produtUrl
	 *            the produt url
	 * @return the string
	 */
	String trimProductUrl(String produtUrl);

	/**
	 * Extract product id from product url.
	 *
	 * @param productPageUrl
	 *            the product page url
	 * @return the string
	 */
	String extractProductId(String productPageUrl);

	/**
	 * Gets the canonical product replacement key.
	 *
	 * @return the canonical product replacement key
	 */
	String getCanonicalProductReplacementKey();

	/**
	 * Gets the normal product url regex.
	 *
	 * @return the normal product url regex
	 */
	REGEXES getNormalProductUrlRegex();

	/**
	 * Gets the canonical product url regex.
	 *
	 * @return the canonical product url regex
	 */
	REGEXES getCanonicalProductUrlRegex();

	/**
	 * Checks if is valid url based on configured filters for domain.
	 *
	 * @param url
	 *            the url
	 * @return true, if is valid url
	 */
	boolean isValidUrl(String url);

	/**
	 * Normalize.
	 *
	 * @param url
	 *            the url
	 * @return the string
	 */
	String normalize(String url);
}
