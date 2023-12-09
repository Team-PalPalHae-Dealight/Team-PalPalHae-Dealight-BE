package com.palpal.dealightbe.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import com.palpal.dealightbe.domain.item.domain.ItemDocumentRepository;
import com.palpal.dealightbe.domain.store.domain.StoreDocumentRepository;

@TestConfiguration
@EnableElasticsearchRepositories(basePackageClasses = {StoreDocumentRepository.class, ItemDocumentRepository.class})
public class ElasticTestContainer extends AbstractElasticsearchConfiguration {


	private static final GenericContainer container;

	static {
		container = new GenericContainer(
			new ImageFromDockerfile()
				.withDockerfileFromBuilder(builder -> {
					builder
						// ES 이미지 가져오기
						.from("docker.elastic.co/elasticsearch/elasticsearch:7.15.2")
						// nori 분석기 설치
						.run("bin/elasticsearch-plugin install analysis-nori")
						.build();
				})
		).withExposedPorts(9200, 9300)
			.withEnv("discovery.type", "single-node");

		container.start();
	}

	@Override
	public RestHighLevelClient elasticsearchClient() {
		// ElasticearchContainer에서 제공해주던 httpHostAddress를 사용할수 없기 때문에
		// 직접 꺼내서 만들어줘야 한다.
		String hostAddress = new StringBuilder()
			.append(container.getHost())
			.append(":")
			.append(container.getMappedPort(9200))
			.toString();

		ClientConfiguration clientConfiguration = ClientConfiguration.builder()
			.connectedTo(hostAddress)
			.build();
		return RestClients.create(clientConfiguration).rest();
	}
}
