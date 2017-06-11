package com.vishalzanzrukia.crawler.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.vishalzanzrukia.crawler.AbstractLifecycleAdapter;
import com.vishalzanzrukia.crawler.parser.AbstractProductParser;
import com.vishalzanzrukia.crawler.parser.ProductParser;
import com.vishalzanzrukia.crawler.urlprocessor.UrlProcessor;
import com.vishalzanzrukia.crawler.urlprocessor.UrlProcessorAdapter;

/**
 * The registry for domain specific components like product-parsers,
 * url-processors, product-url-detectors etc.
 * 
 * @author VishalZanzrukia
 * @see {@link CrawlerComponent}
 * @see {@link UrlProcessor}
 * @see {@link ProductParser}
 * @see {@link UrlProcessorAdapter}
 * @see {@link AbstractProductParser}
 */
public class ComponentRegistry extends AbstractLifecycleAdapter {

	@Autowired
	private SingletonBeanFactory singletonBeanFactory;

	/** The log constant */
	private static final Logger LOG = LogManager.getLogger();

	/** The supported domains. */
	private final Set<String> supportedDomains;

	/** The supported component ids. */
	private final Set<String> supportedComponentIds;

	/** The domain name vs component. */
	private final Map<String, CrawlerComponent> registerMap;

	/**
	 * Instantiates a new component registry.
	 */
	public ComponentRegistry(final Set<String> supportedDomains, final Set<String> supportedComponentIds) {
		registerMap = new HashMap<>();
		this.supportedDomains = supportedDomains;
		this.supportedComponentIds = supportedComponentIds;
	}

	/**
	 * Registers the component for particular domain.
	 *
	 * @param domainName
	 *            the domain name
	 * @param component
	 *            the component for domain
	 */
	public void register(final String domainName, final CrawlerComponent component) {
		final CrawlerComponent existingParser = registerMap.get(domainName);

		if (existingParser != null) {
			throw new UnsupportedOperationException(
					String.format("You can't register the component %s, it's already exist for domain %s", component, domainName));
		}

		LOG.trace("Going to validate given component id component.getComponentId() : {}, {}", component.getComponentId(), supportedComponentIds);

		if (!supportedComponentIds.contains(component.getComponentId())) {
			throw new UnsupportedOperationException(
					String.format("You can't register the component with id %s, as it's not supported yet!, only supported components are %s",
							component.getComponentId(), supportedComponentIds));
		}

		registerMap.put(component.getComponentId() + domainName, component);
		LOG.info(String.format("Successfully registered the component %s", component));
	}

	/**
	 * Retrieve the component from registry.
	 *
	 * @param componentId
	 *            the component id (i.e. {@link UrlProcessor#ID})
	 * @param domainName
	 *            the domain name
	 * @return the zone specific component
	 * 
	 */
	private CrawlerComponent get(final String componentId, final String domainName) {
		final CrawlerComponent component = registerMap.get(componentId + domainName);
		validateComponentExist(domainName, component, componentId);
		LOG.trace("Retrieving the component : {}", component);
		return component;
	}

	@Override
	public int getPhase() {
		/**
		 * this will make sure that spring container will start this bean first
		 * (before starting spring beans)
		 */
		return -1;
	}

	@Override
	public void start() {
		LOG.trace("Going to validate the domain specific components implementations on startup");
		for (final String domainName : supportedDomains) {
			for (final String componentId : supportedComponentIds) {
				validateComponentExist(domainName, registerMap.get(componentId + domainName), componentId);
			}
		}

		if (!supportedDomains.contains(singletonBeanFactory.getRuntimeConfigs().getDomainName())) {
			throw new UnsupportedOperationException(
					String.format("The domain [%s] is not supported by crawler yet!", singletonBeanFactory.getRuntimeConfigs().getDomainName()));
		}
		LOG.debug("Validated all domain specific components registration successfully, total number of registered components : {}",
				registerMap.size());
	}

	/**
	 * Validate whether component exist or not.
	 *
	 * @param domainName
	 *            the domain name
	 * @param component
	 *            the component
	 * @param componentId
	 *            the component id
	 */
	private void validateComponentExist(final String domainName, final CrawlerComponent component, final String componentId) {
		if (component == null) {
			throw new UnsupportedOperationException(
					String.format("There is no component registered for domain name %s and component %s", domainName, componentId));
		}
	}

	/**
	 * Gets the product parser implementation based on zone/domain.
	 *
	 * @return the parser
	 */
	public ProductParser getProductParser() {
		return (ProductParser) get(ProductParser.ID, singletonBeanFactory.getRuntimeConfigs().getDomainName());
	}

	/**
	 * Gets the url processor based on zone/domain.
	 *
	 * @return the url processor
	 */
	public UrlProcessor getUrlProcessor() {
		return (UrlProcessor) get(UrlProcessor.ID, singletonBeanFactory.getRuntimeConfigs().getDomainName());
	}
}
