package com.vishalzanzrukia.crawler.urlprocessor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.vishalzanzrukia.crawler.registry.SingletonBeanFactory;
import com.vishalzanzrukia.crawler.util.CrawlerUtils.REGEXES;

/**
 * The base class for {@link UrlProcessorAdapter} for all domains.
 * 
 * @author VishalZanzrukia
 */
public abstract class AbstractUrlProcessor implements UrlProcessor {

	/** The Constant LOG. */
	private static final Logger LOG = LogManager.getLogger();

	@Autowired
	protected SingletonBeanFactory singletonBeanFactory;

	/**
	 * Gets the trim params.
	 *
	 * @return the trim params
	 */
	abstract List<String> getTrimParams();

	/**
	 * Gets the filter regexes.
	 *
	 * @return the filter regexes
	 */
	abstract List<REGEXES> getFilterRegexes();

	/**
	 * Register the url processors for all domains.
	 */
	@Override
	public void register() {
		LOG.info("Registering the UrlProcessor[{}] for domain name : {}", this.getClass(), getDomainName());
		singletonBeanFactory.getComponentRegistry().register(getDomainName(), this);
	}

	/**
	 * Inits the common filters for all domains, those can't be overwrite by
	 * constructor.
	 */
	protected void initCommonFilters() {
		this.getFilterRegexes().add(REGEXES.JSESSION_ID_URL);
		this.getFilterRegexes().add(REGEXES.MEDIA_URL);
	}

	/**
	 * Inits the default filters, those can be overwrite by constructor for any
	 * particular domain.
	 */
	protected void initDefaultFilters() {
//		this.getFilterRegexes().add(REGEXES.GENERAL_SEARCH_URL);
	}

	/**
	 * Gets the filtered query. It will remove all query params configured with
	 * {@code trimParams}
	 *
	 * @param query
	 *            the query
	 * @return the filtered query
	 */
	protected String getFilteredQuery(final String query) {

		final String[] params = query.split(QUERY_PARAM_SEPARATOR);

		final String filteredQuery = Stream.of(params).map(param -> param.split(PARAM_KEY_VALUE_SEPARATOR)) // split
																											// by
																											// =
																											// to
																											// get
																											// key
																											// value
																											// pairs
																											// for
																											// each
																											// param
				.filter(parts -> parts.length == 2) // making sure to have two
													// have key value pair
				.filter(parts -> !StringUtils.isBlank(parts[0])) // making sure
																	// to have
																	// not null
																	// and empty
																	// key
				.filter(parts -> !StringUtils.isBlank(parts[1])) // making sure
																	// to have
																	// not null
																	// and empty
																	// value
				.filter(parts -> !getTrimParams().contains(parts[0])) // filtering
																		// trimParams
				.map(parts -> parts[0] + PARAM_KEY_VALUE_SEPARATOR + parts[1]) // map
																				// to
																				// key
																				// value
																				// array
																				// to
																				// single
																				// string
																				// parameter
				.collect(Collectors.joining(QUERY_PARAM_SEPARATOR));// joining
																	// parameters

		LOG.trace("Filted query params : {}", filteredQuery);
		return filteredQuery;
	}
}
