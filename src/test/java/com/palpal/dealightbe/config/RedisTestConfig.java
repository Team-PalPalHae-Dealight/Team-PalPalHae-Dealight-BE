package com.palpal.dealightbe.config;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class RedisTestConfig {
	static final String REDIS_IMAGE = "redis:6-alpine";

	static {
		GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
			.withExposedPorts(6379)
			.withReuse(true);

		REDIS_CONTAINER.start();

		System.setProperty("spring.redis.host", REDIS_CONTAINER.getHost());
		System.setProperty("spring.redis.port", REDIS_CONTAINER.getMappedPort(6379).toString());
	}
}
