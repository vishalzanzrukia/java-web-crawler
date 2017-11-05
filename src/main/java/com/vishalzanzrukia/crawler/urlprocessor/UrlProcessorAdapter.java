package com.vishalzanzrukia.crawler.urlprocessor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vishalzanzrukia.crawler.util.CrawlerUtils.REGEXES;

/**
 * This is base class for url specific operations like {@code trim},
 * {@code recognize} etc for all domains( {@code amazon.com} etc.} etc).
 * 
 * @author VishalZanzrukia
 * @see
 */
public class UrlProcessorAdapter extends AbstractUrlProcessor {

	/** The Constant LOG. */
	private static final Logger LOG = LogManager.getLogger();

	/** The domain name */
	private final String domainName;

	/** The canonical product replacement key. */
	private String canonicalProductReplacementKey = "$6";

	/** The normal product url regex. */
	private REGEXES normalProductUrlRegex = REGEXES.GENERAL_PRODUCT_URL;

	/** The canonical product url regex. */
	private REGEXES canonicalProductUrlRegex = REGEXES.GENERAL_CANONICAL_PRODUCT_URL;

	/** The parameters which needs to remove from url, defaults none */
	private List<String> trimParams;

	/** The filter regexes. */
	private List<REGEXES> filterRegexes;

	/**
	 * Instantiates a new url processor adapter with specific configs
	 *
	 * @param domainName
	 *            the domain name
	 * @param canonicalProductReplacementKey
	 *            the canonical product replacement key
	 * @param normalProductUrlRegex
	 *            the normal product url regex
	 * @param canonicalProductUrlRegex
	 *            the canonical product url regex
	 * @param filterRegexes
	 *            the search url regex
	 */
	public UrlProcessorAdapter(final String domainName, final String canonicalProductReplacementKey, final REGEXES normalProductUrlRegex,
			final REGEXES canonicalProductUrlRegex, final List<REGEXES> filterRegexes, List<String> trimParams) {
		this.domainName = domainName;
		this.canonicalProductReplacementKey = canonicalProductReplacementKey;
		this.normalProductUrlRegex = normalProductUrlRegex;
		this.canonicalProductUrlRegex = canonicalProductUrlRegex;
		this.filterRegexes = new ArrayList<>(filterRegexes);
		this.trimParams = new ArrayList<>(trimParams);
		initCommonFilters();
	}
	
	/**
	 * Instantiates a new url processor adapter with all default values
	 *
	 * @param domainName
	 *            the domain name
	 */
	public UrlProcessorAdapter(final String domainName) {
		this.domainName = domainName;
		this.filterRegexes = new ArrayList<>();
		this.trimParams = new ArrayList<>();
		initCommonFilters();
		initDefaultFilters();
	}

	@Override
	public boolean isProductUrl(final String url) {

		final String normalProductUrlRegex = singletonBeanFactory.getCrawlerUtils().getDomainSpecificRegex(getNormalProductUrlRegex());
		final String canonicalProductUrlRegex = singletonBeanFactory.getCrawlerUtils().getDomainSpecificRegex(getCanonicalProductUrlRegex());

		if (url.matches(normalProductUrlRegex)) {
			return true;
		}

		if (url.matches(canonicalProductUrlRegex)) {
			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String trimProductUrl(final String produtUrl) {

		/*final String normalProductUrlRegex = singletonBeanFactory.getCrawlerUtils().getDomainSpecificRegex(getNormalProductUrlRegex());
		final String canonicalProductUrlRegex = singletonBeanFactory.getCrawlerUtils().getDomainSpecificRegex(getCanonicalProductUrlRegex());

		if (produtUrl.matches(normalProductUrlRegex)) {
			LOG.trace("Trimmed product url : {}", produtUrl.replaceAll(normalProductUrlRegex, "$1$3$4"));
			return produtUrl.replaceAll(normalProductUrlRegex, "$1$3$4");
		} else if (produtUrl.matches(canonicalProductUrlRegex)) {
			LOG.trace("The url is canonical product url, so no needs to trim it, returing as it is : {}", produtUrl);
			return produtUrl;
		}

		LOG.warn("The given product url does not match any product url pattern : {}", produtUrl);
		return null;*/
		
		return produtUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String extractProductId(final String productPageUrl) {

		final String normalProductUrlRegex = singletonBeanFactory.getCrawlerUtils().getDomainSpecificRegex(getNormalProductUrlRegex());
		final String canonicalProductUrlRegex = singletonBeanFactory.getCrawlerUtils().getDomainSpecificRegex(getCanonicalProductUrlRegex());

		if (productPageUrl.matches(normalProductUrlRegex)) {
			return productPageUrl.replaceAll(normalProductUrlRegex, "$6");
		} else if (productPageUrl.matches(canonicalProductUrlRegex)) {
			return productPageUrl.replaceAll(canonicalProductUrlRegex, getCanonicalProductReplacementKey());
		}

		LOG.warn("The given product url does not match any product url pattern : {}", productPageUrl);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public REGEXES getNormalProductUrlRegex() {
		return this.normalProductUrlRegex;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public REGEXES getCanonicalProductUrlRegex() {
		return this.canonicalProductUrlRegex;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDomainName() {
		return this.domainName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCanonicalProductReplacementKey() {
		return this.canonicalProductReplacementKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValidUrl(final String url) {

		String filterRegexStr;

		for (REGEXES filterRegex : this.filterRegexes) {
			filterRegexStr = singletonBeanFactory.getCrawlerUtils().getDomainSpecificRegex(filterRegex);
			if (url.matches(filterRegexStr)) {
				LOG.trace("The url [{}] is filtered with regex : {}", url, filterRegex);
				return false;
			}
		}

		LOG.trace("The url [{}] is not filtered with any of configured filters.", url);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String normalize(final String url) {

		LOG.trace("Going to normalize the url : {}", url);
		if (StringUtils.isBlank(url)) {
			return url;
		}

		final String normalizedUrl = singletonBeanFactory.getBasicURLNormalizer().filter(url);
		if (StringUtils.isBlank(normalizedUrl)) {
			LOG.warn("The normalized url is blank for url : {}", url);
			return normalizedUrl;
		}

		if (!trimParams.isEmpty()) {

			if (normalizedUrl.contains(QUERY_SEPARATOR)) {

				final String baseUrl = normalizedUrl.substring(0, normalizedUrl.lastIndexOf(QUERY_SEPARATOR));
				final String queryParams = normalizedUrl.substring(normalizedUrl.lastIndexOf(QUERY_SEPARATOR) + 1);
				final String filteredQuery = getFilteredQuery(queryParams);

				if (StringUtils.isBlank(filteredQuery)) {
					LOG.trace("No query params left after normalize, the filtered query normalized url : {}", baseUrl);
					return baseUrl;
				} else {
					final String filteredQueryNormalizedUrl = baseUrl + QUERY_SEPARATOR + filteredQuery;
					LOG.trace("The filtered query normalized url : {}", filteredQueryNormalizedUrl);
					return filteredQueryNormalizedUrl;
				}
			}

			LOG.trace("The normalized url does not contain any query params : {}", normalizedUrl);
		}

		return normalizedUrl;
	}

	@Override
	public List<String> getTrimParams() {
		return this.trimParams;
	}

	@Override
	public List<REGEXES> getFilterRegexes() {
		return this.filterRegexes;
	}
}
