package com.vishalzanzrukia.crawler.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import com.vishalzanzrukia.crawler.util.CrawlerUtils;
import com.vishalzanzrukia.crawler.util.CrawlerUtils.MESSAGE_HEADERS;

/**
 * This class is responsible to split the product message to multiple required
 * messages.
 *
 * @author VishalZanzrukia
 */
public class ProductSplitter extends AbstractMessageSplitter {

	/** members */
	private CrawlerUtils crawlerUtils;

	/** The Constant LOG. */
	private static final Logger LOG = LogManager.getLogger();

	@Override
	protected List<Message<String>> splitMessage(Message<?> message) {
		final List<String> messages = new ArrayList<>();
		final String productJson = (String) message.getPayload();

		LOG.trace("Inside ProductSplitter.splitMessage : productJson : {}", productJson);
		final String productUrl = (String) message.getHeaders().get(MESSAGE_HEADERS.URL);

		if (productJson == null) {
			LOG.warn("Getting null product inside ProductSplitter.splitMessage, skipping the product : {}", productUrl);
			return Collections.emptyList();
		}

		// TODO : Needs to improve
		/*
		 * final String pidMessage = null; final String priceMessage = null;
		 * final String metaMessage = null;
		 * 
		 * if (pidMessage != null) { messages.add(pidMessage); } else {
		 * LOG.warn("Getting null pid message for this product : {}",
		 * productUrl); }
		 * 
		 * if (priceMessage != null) { messages.add(priceMessage); } else {
		 * LOG.warn("Getting null price message for this product : {}",
		 * productUrl); }
		 * 
		 * if (metaMessage != null) { messages.add(metaMessage); } else {
		 * LOG.warn("Getting null meta message for this product : {}",
		 * productUrl); }
		 * 
		 * if (messages.size() < 3) { LOG.warn(
		 * "Getting any one of [pid, price and meta] or all messages null, so skipping product : {}"
		 * , productUrl); return Collections.emptyList(); }
		 */

		LOG.debug("Pid, price and meta messages has been generated successfully for product : {}", productUrl);

		return messages.stream().map(str -> createMessage(str)).collect(Collectors.toList());
	}

	/**
	 * Creates the message.
	 *
	 * @param messageBody
	 *            the message body
	 * @return the message
	 */
	private Message<String> createMessage(final String messageBody) {
		return MessageBuilder.withPayload(messageBody).build();
	}

	/** getters & setters */

	public CrawlerUtils getCrawlerUtils() {
		return crawlerUtils;
	}

	public void setCrawlerUtils(CrawlerUtils crawlerUtils) {
		this.crawlerUtils = crawlerUtils;
	}

}
