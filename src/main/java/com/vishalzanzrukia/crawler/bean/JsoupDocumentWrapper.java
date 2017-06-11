package com.vishalzanzrukia.crawler.bean;

import java.net.URL;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jsoup.nodes.Document;

/**
 * The model class which can contain some meta-data along with {@link Document}
 * 
 * @author VishalZanzrukia
 */
public class JsoupDocumentWrapper {

	private final Document document;
	private String responseType;
	private URL url;
	private byte[] responseBytes;

	public JsoupDocumentWrapper(final Document document) {
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	// getter setter
	public String getResponseType() {
		return responseType;
	}

	private void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public URL getUrl() {
		return url;
	}

	private void setUrl(URL url) {
		this.url = url;
	}

	public byte[] getResponseBytes() {
		return responseBytes;
	}

	public void setResponseBytes(byte[] responseBytes) {
		this.responseBytes = responseBytes;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * The build class for {@link JsoupDocumentWrapper}.
	 */
	public static class ResponseWrapperBuilder {

		private final Document document;
		private String responseType;
		private URL url;
		private byte[] responseBytes;

		public ResponseWrapperBuilder(final Document document) {
			this.document = document;
		}

		public ResponseWrapperBuilder withResponseType(final String responseType) {
			this.responseType = responseType;
			return this;
		}

		public ResponseWrapperBuilder withURL(final URL url) {
			this.url = url;
			return this;
		}

		public ResponseWrapperBuilder withResponseBytes(final byte[] responseBytes) {
			this.responseBytes = responseBytes;
			return this;
		}

		public JsoupDocumentWrapper build() {
			final JsoupDocumentWrapper wrapper = new JsoupDocumentWrapper(this.document);
			wrapper.setResponseType(this.responseType);
			wrapper.setUrl(url);
			wrapper.setResponseBytes(responseBytes);
			return wrapper;
		}
	}

}
