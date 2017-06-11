package com.vishalzanzrukia.crawler.util;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vishalzanzrukia.crawler.bean.JsoupDocumentWrapper;
import com.vishalzanzrukia.crawler.bean.JsoupDocumentWrapper.ResponseWrapperBuilder;
import com.vishalzanzrukia.crawler.registry.SingletonBeanFactory;

/**
 * The Class PageDownloader.
 * 
 * @author VishalZanzrukia
 */
@Component
public class ContentDownloader implements ContentProvider {

	private static final Logger LOG = LogManager.getLogger();

	@Autowired
	private SingletonBeanFactory singletonBeanFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JsoupDocumentWrapper downloadPage(final String url, boolean bytesNeeded) throws IOException {
		LOG.trace("MaxBytes Config : {}", singletonBeanFactory.getConfigs().getMaxBytes());
		LOG.trace("Timeout Config : {}", singletonBeanFactory.getConfigs().getTimeout());
		LOG.debug("Going to download the content for url : {}", url);

		final Response response = Jsoup.connect(url).parser(Parser.xmlParser()).validateTLSCertificates(false).userAgent("Mozilla")
				.maxBodySize(singletonBeanFactory.getConfigs().getMaxBytes()).timeout(singletonBeanFactory.getConfigs().getTimeout() * 1000)
				.execute();

		if (response.statusCode() == ContentProvider.SC_OK) {

			if (response.parse() != null && response.parse().hasText()) {

				final JsoupDocumentWrapper responseWrapper = new ResponseWrapperBuilder(bytesNeeded ? null : response.parse())
						.withResponseType(response.contentType()).withURL(response.url())
						.withResponseBytes(bytesNeeded ? response.bodyAsBytes() : null).build();
				return responseWrapper;

			} else {

				LOG.error("Retrieving null document for url : {}", url);
				throw new RuntimeException("Retrieving null document for url " + url);
			}
		} else {
			LOG.error("Error ocuured while downloading page content for url : {}, status code : {}", url, response.statusCode());
			throw new RuntimeException("Error ocuured while downloading page content for url : " + url + ", status code : " + response.statusCode());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JsoupDocumentWrapper downloadPage(final String url) throws IOException {
		return downloadPage(url, false);
	}
}
