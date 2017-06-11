package com.vishalzanzrukia.crawler.integration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import com.vishalzanzrukia.crawler.util.CrawlerUtils.MESSAGE_HEADERS;

/**
 * This class is responsible to split the messages from one to multiple
 * 
 * @author VishalZanzrukia
 */
public class UrlSplitter extends AbstractMessageSplitter {

	private static final Logger LOG = LogManager.getLogger();

	@SuppressWarnings("unchecked")
	@Override
	protected List<Message<String>> splitMessage(Message<?> message) {

		LOG.trace("Inside UrlSplitter.splitMessage");
		final Set<String> urls = (Set<String>) message.getPayload();

		final int depth = (int) message.getHeaders().get(MESSAGE_HEADERS.DEPTH);
		LOG.trace("Inside splitMessage, urls[{}] and depth[{}]", urls.size(), depth);

		final List<Message<String>> messages = urls.stream().map(url -> createMessage(url, depth + 1)).collect(Collectors.toList());
		LOG.trace("The number of messages returning by url splitter : {}", messages.size());
		return messages;
	}

	private Message<String> createMessage(final String url, final int depth) {
		return MessageBuilder.withPayload(StringUtils.EMPTY).setHeader(MESSAGE_HEADERS.URL, url).setHeader(MESSAGE_HEADERS.DEPTH, depth).build();
	}
}
