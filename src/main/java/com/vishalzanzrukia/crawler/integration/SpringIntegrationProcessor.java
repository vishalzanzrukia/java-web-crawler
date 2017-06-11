/*
 * 
 */
package com.vishalzanzrukia.crawler.integration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;
import com.vishalzanzrukia.crawler.bean.JsoupDocumentWrapper;
import com.vishalzanzrukia.crawler.bean.Product;
import com.vishalzanzrukia.crawler.registry.SingletonBeanFactory;
import com.vishalzanzrukia.crawler.util.ContentProvider;
import com.vishalzanzrukia.crawler.util.CrawlerUtils.MESSAGE_HEADERS;
import com.vishalzanzrukia.crawler.util.CrawlerUtils.REGEXES;

import redis.clients.jedis.Jedis;

/**
 * <p>
 * IntegrationUtils have all necessary methods used for integration with jms
 * (spring-integration).</BR>
 * </p>
 * 
 * @author VishalZanzrukia
 */
@Component
public class SpringIntegrationProcessor {

	@Autowired
	private SingletonBeanFactory singletonBeanFactory;

	/** Constants. */
	private static final Logger LOG = LogManager.getLogger();
	public static final String KEY_PAGE_CONTENT = "pageContent";
	public static final String KEY_IS_XML_RESPONSE = "isXmlResponse";
	public static final String KEY_RESPONSE_URL = "responseURL";

	/**
	 * Visit the url and download the page content from the internet.
	 *
	 * @param url
	 *            the url
	 * @return the map
	 */
	@ServiceActivator
	public Map<String, Object> visitUrl(@Header(MESSAGE_HEADERS.URL) final String url) {

		singletonBeanFactory.getRateLimiter().acquire();

		LOG.trace("Inside visitUrl : {}", url);
		final JsoupDocumentWrapper response = singletonBeanFactory.getCrawlerUtils().getResponse(url);

		if (response != null) {
			LOG.trace("Visited the url successfully and downloaded the page content : {}", url);

			final Map<String, Object> responseMap = new HashMap<>();
			final String responseType = response.getResponseType();
			LOG.trace("The response type : {} for url : {}", responseType, url);

			if (responseType.contains(ContentProvider.XML_CONTENT_TYPE_DETECTOR)) {
				LOG.debug("Detected xml content type for url : {}", url);
				responseMap.put(KEY_RESPONSE_URL, response.getUrl());
				responseMap.put(KEY_IS_XML_RESPONSE, true);
			} else {
				responseMap.put(KEY_IS_XML_RESPONSE, false);
			}

			responseMap.put(KEY_PAGE_CONTENT, response.getDocument().html());
			return responseMap;
		}

		return null;
	}

	/**
	 * Parses the xml urls.
	 *
	 * @param responseMap
	 *            the response map
	 * @param url
	 *            the url
	 * @return the sets the
	 */
	@Transformer
	public Set<String> parseXmlUrls(@Payload final Map<String, Object> responseMap, @Header(MESSAGE_HEADERS.URL) final String url) {
		return singletonBeanFactory.getCrawlerUtils().parseXmlUrls(responseMap);
	}

	/**
	 * Parses the urls from page content received from payload.
	 *
	 * @param payload
	 *            the page content
	 * @return the sets the
	 */
	@Transformer
	public Set<String> parseUrls(@Payload Map<String, Object> responseMap, @Header(MESSAGE_HEADERS.URL) final String url) {

		final Document document = Jsoup.parse((String) responseMap.get(KEY_PAGE_CONTENT));
		final Set<String> returnSet;
		if (document == null) {
			returnSet = Collections.emptySet();
		} else {
			if (document.baseUri() == null || document.baseUri().trim().equals(StringUtils.EMPTY)) {
				LOG.trace("The base uri setting for document : {}", url);
				document.setBaseUri(url);
			}
			returnSet = singletonBeanFactory.getComponentRegistry().getProductParser().parseUrls(document);
		}
		LOG.trace("Inside IntegrationUtils.parseUrls, url size : {}", returnSet.size());
		return returnSet;
	}

	/**
	 * Checks if is product url or not, it will also consider the canonical
	 * product urls.<BR>
	 * So it will return true if the given url is normal product url (which
	 * contains <code>product/index.jsp?productId={digit}</code>) or the
	 * canonical product url (which contains <code>/buy</code> and ends with
	 * <code>-{digit}</code>)
	 *
	 * @param url
	 *            the url
	 * @return true, if is product url
	 */
	@ServiceActivator
	public boolean isProductUrl(@Header(MESSAGE_HEADERS.URL) final String url) {
		LOG.trace("Inside SiteHelper.isProductUrl url : {}", url);
		final boolean isProductUrl = singletonBeanFactory.getComponentRegistry().getUrlProcessor().isProductUrl(url);
		LOG.trace("isProductUrl : {}", isProductUrl);
		return isProductUrl;
	}

	/**
	 * Parses the product.
	 *
	 * @param url
	 *            the url
	 * @return the string
	 */
	@ServiceActivator
	public String parseProduct(@Header(MESSAGE_HEADERS.URL) final String url) {
		LOG.trace("Inside SiteHelper.parseProduct url : {}", url);
		final Product product = singletonBeanFactory.getComponentRegistry().getProductParser().parseProduct(url);
		if (product == null) {
			LOG.warn("Returning null product for url : {}", url);
			return null;
		}
		return singletonBeanFactory.getCrawlerUtils().serializeProduct(product);
	}

	/**
	 * It will check whether product should be parsed or not based on already
	 * parsed products history.<BR>
	 * If there is no entry in database for given product, it will store into
	 * database and will returns <code>true</true> <B>NOTE:</B>This method will
	 * be called for both types or URLs the canonical one and the normal one.
	 *
	 * @param productUrl
	 *            the url
	 * @return true, if successful
	 */
	@Filter
	public boolean shouldParse(@Header(MESSAGE_HEADERS.URL) final String productUrl) {

		LOG.trace("Inside shouldParse url : {}", productUrl);

		final String productId = singletonBeanFactory.getComponentRegistry().getUrlProcessor().extractProductId(productUrl);
		LOG.trace("Product id extracted from url : {}", productId);

		if (productId == null) {

			LOG.warn("Crawler could not able to extract productId from product page url : {}", productUrl);
			return false;

		} else {

			try (Jedis client = getRedisClient()) {

				if (client.sismember(singletonBeanFactory.getRuntimeConfigs().getParsedProductsRedisKey(), productId)) {

					LOG.trace("The productId found in database, so crawler will not parse the product again : {}", productId);
					return false;

				}
			}

			persistInRedis(singletonBeanFactory.getRuntimeConfigs().getParsedProductsRedisKey(), productId);
			LOG.trace("The productId does not found in database, crawler will parse the product : {}", productId);

			return true;
		}
	}

	/**
	 * It checks whether system needs to download page content or not for given
	 * url
	 *
	 * @param depth
	 *            the depth
	 * @param url
	 *            the url
	 * @return true, if successful
	 */
	@Filter
	public boolean shouldVisit(@Header(MESSAGE_HEADERS.DEPTH) final int depth, @Header(MESSAGE_HEADERS.URL) final String url) {

		LOG.trace("Inside shouldVisit url : {}", url);

		if (depth >= singletonBeanFactory.getConfigs().getMaxDepth()) {
			return false;
		}
		if (!url.matches(singletonBeanFactory.getCrawlerUtils().getDomainSpecificRegex(REGEXES.VALID_URL))) {
			return false;
		}
		if (!singletonBeanFactory.getComponentRegistry().getUrlProcessor().isValidUrl(url)) {
			return false;
		}
		if (!singletonBeanFactory.getRobotsTxtParser().isAllow(url)) {
			return false;
		}

		try (Jedis client = getRedisClient()) {
			if (client.sismember(singletonBeanFactory.getRuntimeConfigs().getVisitedUrlRedisKey(),
					singletonBeanFactory.getCrawlerUtils().trimHttps(url))) {
				return false;
			}
		}
		try (Jedis client = getRedisClient()) {
			if (client.sismember(singletonBeanFactory.getRuntimeConfigs().getVisitedUrlRedisKey(),
					singletonBeanFactory.getCrawlerUtils().trimHttps(singletonBeanFactory.getCrawlerUtils().trimCategoryUrl(url)))) {
				return false;
			}
		}

		if (singletonBeanFactory.getCrawlerUtils().isCategoryUrl(url)) {
			persistInRedis(singletonBeanFactory.getRuntimeConfigs().getVisitedUrlRedisKey(),
					singletonBeanFactory.getCrawlerUtils().trimCategoryUrl(url));
		} else {
			persistInRedis(singletonBeanFactory.getRuntimeConfigs().getVisitedUrlRedisKey(), url);
		}

		LOG.trace("Inside shouldVisit, needs to visit url : {}", url);
		return true;
	}

	/**
	 * Remove extra parameters except productId from product url
	 *
	 * @param url
	 *            the url
	 * @return the string
	 */
	public String trimProductUrl(@Header(MESSAGE_HEADERS.URL) final String url) {
		final String trimmedProductUrl = singletonBeanFactory.getComponentRegistry().getUrlProcessor().trimProductUrl(url);
		LOG.trace("Inside trimProductUrl, trimmed product url : {}", trimmedProductUrl);
		return trimmedProductUrl;
	}

	/**
	 * Persist in redis.
	 *
	 * @param key
	 *            the key
	 * @param data
	 *            the data
	 */
	private void persistInRedis(final String key, final String data) {
		try (Jedis client = getRedisClient()) {
			client.sadd(key, singletonBeanFactory.getCrawlerUtils().trimHttps(data));
		}
	}

	/**
	 * Gets the redis client.
	 *
	 * @return the redis client
	 */
	@VisibleForTesting
	Jedis getRedisClient() {
		return singletonBeanFactory.getJedisPool().getResource();
	}
}
