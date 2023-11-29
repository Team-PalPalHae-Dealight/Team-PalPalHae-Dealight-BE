package com.palpal.dealightbe.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import lombok.RequiredArgsConstructor;

@EnableElasticsearchRepositories
@RequiredArgsConstructor
@Configuration
public class ElasticSearchConfig extends AbstractElasticsearchConfiguration {

	private final ElasticSearchProperty elasticSearchProperty;

	@Override
	public RestHighLevelClient elasticsearchClient() {
		String host = elasticSearchProperty.getHost();
		int port = elasticSearchProperty.getPort();
		ClientConfiguration clientConfiguration = ClientConfiguration.builder()
			.connectedTo(host + ":" + port)
			.build();

		return RestClients.create(clientConfiguration)
			.rest();
	}
}
