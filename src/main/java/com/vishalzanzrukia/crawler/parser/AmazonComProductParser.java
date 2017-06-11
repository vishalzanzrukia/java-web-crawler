package com.vishalzanzrukia.crawler.parser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import com.google.common.annotations.VisibleForTesting;

/**
 * The HTML parser for product details page of <code>amazon.com</code>
 * 
 * @author VishalZanzrukia
 */
public class AmazonComProductParser extends AbstractProductParser {

	private static final Logger LOG = LogManager.getLogger();

	/** The domain name */
	private final String domainName;

	/**
	 * Instantiates a new page parser.
	 */
	public AmazonComProductParser(final String domainName) {
		super();
		LOG.trace("Constructing AtProductParser");
		this.domainName = domainName;
	}

	/**
	 * {@inheritDoc}
	 */
	@VisibleForTesting
	@Override
	String parseBarcode(final Document document, final String productId) {
		final String barcode = "01234567";
		// TODO : Needs to implement barcode parsing logic for amazon.com
		return barcode;
	}

	/**
	 * {@inheritDoc}
	 */
	@VisibleForTesting
	@Override
	Integer parseWeight(final Document document, final String productId) {
		String weight = "30";
		// TODO : Needs to implement weight parsing logic for amazon.com
		return Integer.parseInt(weight);
	}

	/**
	 * {@inheritDoc}
	 */
	@VisibleForTesting
	@Override
	List<Integer> parseDimensions(final Document document, final String productId) {
		final List<Integer> dimensions = new ArrayList<>();
		// TODO : Needs to implement dimensions parsing logic for amazon.com
		return dimensions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BigDecimal parsePrice(final Document document, final String productId) {
		// TODO : Needs to implement price parsing logic for amazon.com
		return new BigDecimal(10);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@VisibleForTesting
	String parseTitle(Document document, final String productId) {
		final String title = "Product Titel";
		// TODO : Needs to implement price parsing logic for amazon.com
		return title;
	}

	@Override
	public String getDomainName() {
		return this.domainName;
	}
}
