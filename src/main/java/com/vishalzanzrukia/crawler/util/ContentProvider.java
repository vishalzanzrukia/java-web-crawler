package com.vishalzanzrukia.crawler.util;

import java.io.IOException;

import org.jsoup.nodes.Document;

import com.vishalzanzrukia.crawler.bean.JsoupDocumentWrapper;

/**
 * The interface to provide the {@link JsoupDocumentWrapper} based on
 * {@code pageUrl}
 * 
 * @author VishalZanzrukia
 */
public interface ContentProvider {

	public static final int SC_OK = 200;
	public static final String XML_CONTENT_TYPE_DETECTOR = "/xml";

	/**
	 * Download page and provide the {@link JsoupDocumentWrapper} with necessary
	 * metadata.<BR>
	 * It will call {@link #downloadPage(String, boolean)} with
	 * {@code url,false} values.
	 *
	 * @param url
	 *            the page url
	 * @return the response wrapper {@link JsoupDocumentWrapper}
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	JsoupDocumentWrapper downloadPage(final String url) throws IOException;

	/**
	 * Download page and provide the {@link JsoupDocumentWrapper} with necessary
	 * metadata.<BR>
	 * if {@code bytesNeeded} value is {@code true}, then
	 * {@link JsoupDocumentWrapper#getResponseBytes()} will return actual
	 * content bytes, and {@link JsoupDocumentWrapper#getDocument()} will return
	 * null<BR>
	 * else {@link JsoupDocumentWrapper#getDocument()} will return
	 * {@link Document} with actual html document and
	 * {@link JsoupDocumentWrapper#getResponseBytes()} will return null.
	 *
	 * @param url
	 *            the url
	 * @param bytesNeeded
	 *            if {@code true}, then
	 *            {@link JsoupDocumentWrapper#getResponseBytes()} will return
	 *            actual content byte and
	 *            {@link JsoupDocumentWrapper#getDocument()} will return null
	 *            <BR>
	 *            else {@link JsoupDocumentWrapper#getDocument()} will return
	 *            {@link Document} with actual html document and
	 *            {@link JsoupDocumentWrapper#getResponseBytes()} will return
	 *            null.
	 * @return the response wrapper
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	JsoupDocumentWrapper downloadPage(final String url, boolean bytesNeeded) throws IOException;
}
