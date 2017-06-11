package com.vishalzanzrukia.crawler.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.vishalzanzrukia.crawler.bean.JsoupDocumentWrapper;
import com.vishalzanzrukia.crawler.bean.Product;
import com.vishalzanzrukia.crawler.integration.SpringIntegrationProcessor;
import com.vishalzanzrukia.crawler.registry.SingletonBeanFactory;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapURL;
import crawlercommons.sitemaps.UnknownFormatException;

/**
 * The Class CrawlerUtils.
 * 
 * @author VishalZanzrukia
 */
@Component
public class CrawlerUtils {

	@Autowired
	private SingletonBeanFactory singletonBeanFactory;

	private static final Logger LOG = LogManager.getLogger();
	// TODO : Needs to improve this
	private static final File ERROR_FILE = new File("error_products.txt");
	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * Used to clean the dollar string
	 */
	private static final Pattern DOUBLE_CLEANER_DOLLAR = Pattern.compile("[$]\\p{javaSpaceChar}?");

	public static final String LINE_SEPARATOR;

	static {
		final StringBuilderWriter buf = new StringBuilderWriter(4);
		final PrintWriter out = new PrintWriter(buf);
		out.println();
		LINE_SEPARATOR = buf.toString();
		out.close();
	}

	/**
	 * The class which contains all jms message headers used while crawling
	 * flow.
	 */
	public static class MESSAGE_HEADERS {
		public static final String DEPTH = "depth";
		public static final String URL = "url";
		public static final String TRIMMED_PRODUCT_URL = "trimmedProductUrl";
	}

	/**
	 * Different types of URL regexed used within application.
	 */
	public enum REGEXES {

		/**
		 * The domain specific valid url, if this will not match it will be
		 * filterd
		 */
		VALID_URL("^" + REGEXES.DOMAIN_BASE_REGEX + "(.*)$"),

		/** Filter urls for media types */
		MEDIA_URL("(?i)^" + REGEXES.DOMAIN_BASE_REGEX + "(.*\\.)(apk|gif|jpg|png|ico|css|sit|eps|wmf|rar|tar|zip|rpm|tgz|mov|exe|jpeg|bmp|js|mpg|mp3|mp4|ogv|pdf)(\\?|&|$)"),

		/** Filter url for jsessionid */
		JSESSION_ID_URL("(?i)^" + REGEXES.DOMAIN_BASE_REGEX + "(.*jsessionid\\=.*)$"),

		/** Filter url for write review page */
		WRITE_REVIEW_URL("^" + REGEXES.DOMAIN_BASE_REGEX + "(.*\\/writeReview.jsp\\?)(.*)$"),

		/** Filter url for assets */
		ASSETS_URL("^" + REGEXES.DOMAIN_BASE_REGEX + "(assets\\/)(.*)$"),

		/** Filter mobile urls */
		MOBILE_URL("^" + REGEXES.DOMAIN_BASE_REGEX + "(mobile\\/)(.*)$"),

		/** Filter url for account */
		ACCOUNT_URL("^" + REGEXES.DOMAIN_BASE_REGEX + "(account\\/)(.*)$"),

		/** Filter search url for most of domains */
		GENERAL_SEARCH_URL("^" + REGEXES.DOMAIN_BASE_REGEX + "(search)(\\?|\\/)(.*)$"),

		/**
		 * The canonical product url for most of domains, some domain might have
		 * specific regex
		 */
		GENERAL_CANONICAL_PRODUCT_URL("^" + REGEXES.DOMAIN_BASE_REGEX + "(buy\\/)(.*)(\\-)((\\d)+)$"),

		/**
		 * The normal product url for most of domains, some domain (like co.uk)
		 * might have specific regex
		 */
		GENERAL_PRODUCT_URL("^" + REGEXES.DOMAIN_BASE_REGEX + "(product\\/index\\.jsp\\?productId\\=)((\\d)+)(.*)$"),

		/** The normal product url for co.uk domain */
		UK_PRODUCT_URL("^" + REGEXES.DOMAIN_BASE_REGEX + "(pdp\\/product\\.jsp\\?productId\\=)([0-9A-Z]+)(.*)$"),

		/** category normal url regex */
		CATEGORY_URL("^" + REGEXES.DOMAIN_BASE_REGEX + "((?!search\\b)\\b\\w+\\/index\\.jsp\\?)(.*)(categoryId\\=(\\d)+)(.*)$"),

		/** category page url regex */
		CATEGORY_PAGE_URL("^" + REGEXES.DOMAIN_BASE_REGEX + "((?!search\\b)\\b\\w+\\/index\\.jsp\\?)(.*)(categoryId\\=(\\d)+)(((?!page).)*)(page\\=(\\d)+)?(.*)$");

		public static final String DOMAIN_BASE_REGEX = "(http(s)?\\:\\/\\/www\\.%s\\/)";

		private String regex;

		private REGEXES(final String regex) {
			this.regex = regex;
		}
		public String getRegex() {
			return regex;
		}
	}

	/**
	 * Gets the domain specific regex.
	 *
	 * @param regex
	 *            the regex
	 * @return the domain specific regex
	 */
	public String getDomainSpecificRegex(REGEXES regex) {
		return String.format(regex.getRegex(), singletonBeanFactory.getRuntimeConfigs().getDomainName());
	}

	/**
	 * Gets the response.
	 *
	 * @param url
	 *            the url
	 * @return the response
	 */
	public JsoupDocumentWrapper getResponse(final String url) {
		return getResponse(url, new int[]{1});
	}

	/**
	 * Gets the jsoup document.
	 *
	 * @param url
	 *            the url
	 * @return the document
	 */
	@VisibleForTesting
	JsoupDocumentWrapper getResponse(final String url, int[] noOfRetries) {
		try {
			return singletonBeanFactory.getContentProvider().downloadPage(url);
		} catch (Exception e) {
			if (noOfRetries[0] >= singletonBeanFactory.getConfigs().getMaxRetry()) {
				LOG.error("Error while retrieving Jsoup document from url : {}", url, e);
			} else {
				noOfRetries[0] = ++noOfRetries[0];
				LOG.info("Error while retrieving Jsoup document from url : {}, trying {}th time.", url, noOfRetries[0]);
				return getResponse(url, noOfRetries);
			}
		}
		LOG.warn("Returning null document for url : {}", url);
		return null;
	}

	/**
	 * Writes the string into file.
	 *
	 * @param file
	 *            the file
	 * @param data
	 *            the product url
	 */
	public void write(final File file, final String data) {
		try {
			FileUtils.writeStringToFile(file, data, Charset.defaultCharset(), true);
		} catch (IOException e) {
			LOG.error("Error while writting data into error file", e);
		}
	}

	/**
	 * Write error.
	 *
	 * @param data
	 *            the data
	 */
	// TODO : Needs to improve by removing this
	public void writeError(final String data) {
		write(ERROR_FILE, data);
	}

	/**
	 * Takes a String representing an amount in US dollars and returns a Double
	 * value.
	 *
	 * @param dollars
	 *            String representing a US dollar amount
	 * @return The parsed value
	 * @throws NumberFormatException
	 *             If the value is unable to be parsed
	 */
	public BigDecimal dollarsToBigDecimal(String dollars) {
		BigDecimal output;
		if (dollars != null && dollars.toLowerCase().contains("free")) {
			output = new BigDecimal("0");
		} else {
			output = parseCurrency(dollars);
		}
		return output;
	}

	/**
	 * Trim category url.
	 *
	 * @param url
	 *            the url
	 * @return the string
	 */
	public String trimCategoryUrl(final String url) {
		/** Assuming that `page=` is not used for any other purpose in URL */
		if (url.contains("page=")) {
			return url.replaceAll(getDomainSpecificRegex(REGEXES.CATEGORY_PAGE_URL), "$1$3$5$8$9");
		} else {
			return url.replaceAll(getDomainSpecificRegex(REGEXES.CATEGORY_URL), "$1$3$5");
		}
	}

	/**
	 * Checks if is category url.
	 *
	 * @param url
	 *            the url
	 * @return true, if is category url
	 */
	public boolean isCategoryUrl(final String url) {
		return url.matches(getDomainSpecificRegex(REGEXES.CATEGORY_URL));
	}

	/**
	 * Serialize product.
	 *
	 * @param product
	 *            the product
	 * @return the string
	 */
	public String serializeProduct(final Product product) {
		try {
			return MAPPER.writeValueAsString(product);
		} catch (Exception e) {
			LOG.error("Error while retrieving product json from model : {}", product, e);
			throw new RuntimeException("Error while retrieving product json from model : " + product, e);
		}
	}

	/**
	 * Translate product.
	 *
	 * @param productJson
	 *            the product json
	 * @return the crawler product model
	 */
	public Product translateProduct(final String productJson) {
		try {
			return MAPPER.readValue(productJson, Product.class);
		} catch (Exception e) {
			LOG.error("Error while retrieving product model from json : {}", productJson, e);
			throw new RuntimeException("Error while retrieving product model from json : " + productJson);
		}
	}

	/**
	 * Pound to grams.
	 *
	 * @param pounds
	 *            the pounds
	 * @return the double
	 */
	public int poundToGrams(final double pounds) {
		final int grams = new BigDecimal(pounds).multiply(new BigDecimal(0.45359237)).multiply(new BigDecimal(1000)).intValue();
		return grams;
	}

	/**
	 * Kg to grams.
	 *
	 * @param kg
	 *            the kg
	 * @return the int
	 */
	public int kgToGrams(final double kg) {
		final int grams = new BigDecimal(kg).multiply(new BigDecimal(1000)).intValue();
		return grams;
	}

	/**
	 * Inch to millimetre.
	 *
	 * @param inch
	 *            the inch
	 * @return the double
	 */
	public int inchToMillimetre(final double inch) {
		int millimetre = new BigDecimal(inch).multiply(new BigDecimal(25.4)).intValue();
		return millimetre;
	}

	/**
	 * Cm to millimetre.
	 *
	 * @param cm
	 *            the cm
	 * @return the int
	 */
	public int cmToMillimetre(final double cm) {
		int millimetre = new BigDecimal(cm).multiply(new BigDecimal(10)).intValue();
		return millimetre;
	}

	/**
	 * Trim https to http
	 *
	 * @param url
	 *            the url
	 * @return the string
	 */
	public String trimHttps(final String url) {
		if (url.startsWith("https")) {
			final String trimmedHttpUrl = "http" + "://" + url.split("\\:\\/\\/")[1];
			return trimmedHttpUrl;
		} else {
			return url;
		}
	}

	/**
	 * Provides the time different in minutes between given past time and the
	 * current time.
	 *
	 * @param millis
	 *            the past time in millis
	 * @return the long the differece in minutes
	 */
	public long diffInMins(long millis) {
		return ((System.currentTimeMillis() - millis) / (1000 * 60));
	}

	/**
	 * Provides the time different in seconds between given past time and the
	 * current time.
	 *
	 * @param millis
	 *            the past time in millis
	 * @return the long the differece in seconds
	 */
	public long diffInSeconds(long millis) {
		return ((System.currentTimeMillis() - millis) / 1000);
	}

	/**
	 * Parses the xml urls.
	 *
	 * @param responseMap
	 *            the response map
	 * @return the sets the
	 */
	public Set<String> parseXmlUrls(final Map<String, Object> responseMap) {

		Set<String> parsedUrls = Collections.emptySet();
		final URL url = (URL) responseMap.get(SpringIntegrationProcessor.KEY_RESPONSE_URL);
		final String pageContent = (String) responseMap.get(SpringIntegrationProcessor.KEY_PAGE_CONTENT);
		try {

			final AbstractSiteMap siteMap = singletonBeanFactory.getSiteMapParser().parseSiteMap(pageContent.getBytes(), url);
			LOG.trace("Inside parseXmlUrls, isIndex : {}, type : {}, isProcessed : {}", siteMap.isIndex(), siteMap.getType(), siteMap.isProcessed());

			if (siteMap instanceof SiteMap) {

				Collection<SiteMapURL> urls = ((SiteMap) siteMap).getSiteMapUrls();
				LOG.debug("Found total {} nummber of urls while parsing xml url : {}", urls.size(), url);

				if (urls.size() > 0) {
					parsedUrls = urls.stream().map(siteMapUrl -> siteMapUrl.getUrl().toString())
							.map(urlIn -> singletonBeanFactory.getComponentRegistry().getUrlProcessor().normalize(urlIn)).collect(Collectors.toSet());
				}
			} else if (siteMap instanceof SiteMapIndex) {

				Collection<AbstractSiteMap> siteMaps = ((SiteMapIndex) siteMap).getSitemaps();
				LOG.debug("Found total {} nummber of sitemap urls while parsing xml url : {}", siteMaps.size(), url);

				if (siteMaps.size() > 0) {
					parsedUrls = siteMaps.stream().map(siteMapIn -> siteMapIn.getUrl().toString()).collect(Collectors.toSet());
				}
			} else {
				LOG.warn("AbstractSiteMap must be instance of either SiteMap or SiteMapIndex, check if API changed?");
			}

		} catch (IOException | UnknownFormatException e) {
			LOG.error("Error while parsing urls from xml response retrieved from url : {}", url, e);
		}

		return parsedUrls;
	}

	/**
	 * Private helper method to strip out unused characters prior to parsing a
	 * double value.
	 */
	private String doubleStringCleaner(String value) {
		return DOUBLE_CLEANER_DOLLAR.matcher(value).replaceAll("");
	}

	/**
	 * Takes a String representing an amount in the specified locale and returns
	 * a Double value.
	 *
	 * @param value
	 *            String representing a US dollar amount
	 * @return The parsed value
	 * @throws NumberFormatException
	 *             If the value is unable to be parsed
	 */
	private BigDecimal parseCurrency(String value) {
		return new BigDecimal(doubleStringCleaner(value).trim());
	}
}
