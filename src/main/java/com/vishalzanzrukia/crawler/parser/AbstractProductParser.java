package com.vishalzanzrukia.crawler.parser;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;
import com.vishalzanzrukia.crawler.bean.JsoupDocumentWrapper;
import com.vishalzanzrukia.crawler.bean.Product;
import com.vishalzanzrukia.crawler.registry.SingletonBeanFactory;

/**
 * This is base class for product page parser for all domains/zones
 * 
 * @author VishalZanzrukia
 */
public abstract class AbstractProductParser implements ProductParser {

	/** The Constant LOG. */
	private static final Logger LOG = LogManager.getLogger();

	@Autowired
	protected SingletonBeanFactory singletonBeanFactory;

	/**
	 * Register the product parsers for all domains
	 */
	@Override
	public void register() {
		LOG.info("Registering the parser[{}] for zone-suffix : {}", this.getClass(), getDomainName());
		singletonBeanFactory.getComponentRegistry().register(getDomainName(), this);
	}

	/**
	 * Process links from current page.
	 *
	 * @param document
	 *            the document
	 * @return the sets the
	 */
	public Set<String> parseUrls(final Document document) {
		LOG.trace("Base URI from document : {}", document.baseUri());
		final Set<String> urls = document.select("a[href]").stream()
				.map(link -> link.attr(
						"abs:href")) /** it will handle absolute url as well */
				.map(link -> singletonBeanFactory.getComponentRegistry().getUrlProcessor().normalize(link))
				.filter(link -> !StringUtils.isBlank(
						link)) /**
								 * to filter javascript function calls from href
								 */
				.collect(Collectors.toCollection(LinkedHashSet::new));

		return urls;
	}

	/**
	 * Parses the product.
	 *
	 * @param productPageUrl
	 *            the product page url
	 * @return the model
	 */
	public Product parseProduct(final String productPageUrl) {
		final JsoupDocumentWrapper response = singletonBeanFactory.getCrawlerUtils().getResponse(productPageUrl);
		if (response == null) {
			LOG.warn("Retrieved null document for url : {}", productPageUrl);
			return null;
		}
		return parseProduct(response.getDocument(), productPageUrl);
	}

	/**
	 * Parses the product.
	 *
	 * @param document
	 *            the document
	 * @param productPageUrl
	 *            the product page url
	 * @return the model
	 */
	@VisibleForTesting
	Product parseProduct(final Document document, final String productPageUrl) {
		return parseProduct(productPageUrl, document, new int[]{1});
	}

	private Product parseProduct(final String productPageUrl, final int[] noOfRetries, final Document document) {
		noOfRetries[0] = ++noOfRetries[0];
		return parseProduct(productPageUrl, document, noOfRetries);
	}

	/**
	 * Process the product.
	 *
	 * @param productPageUrl
	 *            the product page url
	 * @param document
	 *            the document
	 * @param noOfRetries
	 *            the no of retries
	 * @return the model
	 */
	@VisibleForTesting
	Product parseProduct(final String productPageUrl, final Document document, final int[] noOfRetries) {
		String productId = null;

		try {

			if (document != null) {

				LOG.trace("Parsing : {}", productPageUrl);
				Product product = null;

				productId = singletonBeanFactory.getComponentRegistry().getUrlProcessor().extractProductId(productPageUrl);
				if (productId == null) {
					LOG.error("Not able to extract productId from product url : {}", productPageUrl);
					throw new IllegalArgumentException("Not able to extract productId from product url : " + productPageUrl);
				}

				final String barcode = parseBarcode(document, productId);
				final BigDecimal price = parsePrice(document, productId);
				final Integer weight = parseWeight(document, productId);

				final String description = parseDescription(document);
				final String title = parseTitle(document, productId);
				final List<Integer> dimensions = parseDimensions(document, productId);
				final String keywords = parseKeywords(document);

				if (barcode == null && price == null && weight == null && dimensions.size() == 0) {

					LOG.warn(
							"Not able to extract barcode, price, weight and dimensions related information for the product : {}, it seems not a product page, skipping to parse!",
							productId);

				} else if (barcode == null || price == null || weight == null || dimensions.size() == 0) {

					if (noOfRetries[0] >= singletonBeanFactory.getConfigs().getMaxRetry()) {

						LOG.warn("We have tried to extract metadata for {} times of the product {}", singletonBeanFactory.getConfigs().getMaxRetry(),
								productId);
						singletonBeanFactory.getCrawlerUtils().writeError(productPageUrl + "\n");

					} else {

						LOG.info("Trying to parse product {}, {}th time.", productId, (noOfRetries[0] + 1));
						return parseProduct(productPageUrl, noOfRetries, document);
					}

				} else {

					LOG.debug("Extracted the product {} with barcode {} and price {}", productId, barcode, price);
					product = new Product(productId, title, description, keywords, barcode, price, weight, dimensions);

					LOG.trace("Product processed successfully : {}", product);
					return product;
				}
			}

		} catch (Exception e) {

			// TODO : Needs to improve to write error msgs
			LOG.error("Error while parsing product url : " + productPageUrl, e);
			singletonBeanFactory.getCrawlerUtils().writeError(productPageUrl + "\n");

		}

		LOG.warn("Returning null product for id : {}", productId);
		return null;
	}

	/**
	 * Gets the metadata property value.
	 *
	 * @param document
	 *            the document
	 * @param identifierKey
	 *            the identifier key
	 * @param propertyName
	 *            the property name
	 * @return the metadata property value
	 */
	protected String getMetadataPropertyValue(final Document document, final String identifierKey, final String propertyName) {
		final Elements elements = getElements(document, "meta[" + identifierKey + "='" + propertyName + "']");
		if (elements.size() > 0) {
			return elements.first().attr("content");
		}
		return null;
	}

	/**
	 * Gets the price elements.
	 *
	 * @param document
	 *            the document
	 * @param selector
	 *            the selector
	 * @return the price elements
	 */
	protected Elements getElements(final Document document, final String selector) {
		return document.select(selector);
	}

	/**
	 * Parses the description.
	 *
	 * @param document
	 *            the document
	 * @return the string
	 */
	protected String parseDescription(final Document document) {
		final String description = getMetadataPropertyValue(document, "name", "description");
		return description;
	}

	/**
	 * Parses the keywords.
	 *
	 * @param document
	 *            the document
	 * @return the string
	 */
	protected String parseKeywords(final Document document) {
		final String keywords = getMetadataPropertyValue(document, "name", "keywords");
		return keywords;
	}

	/**
	 * Parses the barcode.
	 *
	 * @param document
	 *            the document
	 * @param productId
	 *            the product id
	 * @return the string the barcode
	 */
	abstract String parseBarcode(final Document document, final String productId);

	/**
	 * Parses the weight.
	 *
	 * @param document
	 *            the document
	 * @param productId
	 *            the product id
	 * @return the int the weight
	 */
	abstract Integer parseWeight(final Document document, final String productId);

	/**
	 * Parses the dimensions.
	 *
	 * @param document
	 *            the document
	 * @param productId
	 *            the product id
	 * @return the dimensions
	 */
	abstract List<Integer> parseDimensions(final Document document, final String productId);

	/**
	 * Parses the price.
	 *
	 * @param document
	 *            the document
	 * @param productId
	 *            the product id
	 * @return the big decimal
	 */
	abstract BigDecimal parsePrice(final Document document, final String productId);

	/**
	 * Parses the title.
	 *
	 * @param document
	 *            the document
	 * @param productId
	 *            the product id
	 * @return the string
	 */
	abstract String parseTitle(final Document document, final String productId);
}
