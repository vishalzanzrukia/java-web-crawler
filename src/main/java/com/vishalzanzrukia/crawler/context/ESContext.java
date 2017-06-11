package com.vishalzanzrukia.crawler.context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.vishalzanzrukia.crawler.esrepository")
public class ESContext {

	private static Logger LOG = LoggerFactory.getLogger(ESContext.class);

	@Value("${elasticsearch.home}")
	private String elasticsearchHome;

	@Bean
	public Client esClient() {
		try {
			final Path tmpDir = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "elasticsearch_data");
			LOG.debug("Teamp dir path : " + tmpDir.toAbsolutePath().toString());

			// @formatter:off

            final Settings.Builder elasticsearchSettings =
                    Settings.settingsBuilder().put("http.enabled", "false")
                                              .put("path.data", tmpDir.toAbsolutePath().toString())
                                              .put("path.home", elasticsearchHome);
            
            return new NodeBuilder()
                    .local(true)
                    .settings(elasticsearchSettings)
                    .node()
                    .client();
            
            // @formatter:on
		} catch (final IOException ioex) {
			LOG.error("Cannot create temp dir", ioex);
			throw new RuntimeException(ioex);
		}
	}

	@Bean
	public ElasticsearchOperations elasticsearchTemplate(@Qualifier("esClient") final Client esClient) {
		return new ElasticsearchTemplate(esClient);
	}
}
