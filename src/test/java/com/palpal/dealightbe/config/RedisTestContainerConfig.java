//package com.palpal.dealightbe.config;
//
//import org.junit.jupiter.api.extension.BeforeAllCallback;
//import org.junit.jupiter.api.extension.ExtensionContext;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.utility.DockerImageName;
//
//@Testcontainers
//public class RedisTestContainerConfig implements BeforeAllCallback {
//
//	private static final String REDIS_IMAGE = "redis:7.0.8-alpine";
//	private static final int REDIS_PORT = 6379;
//	private GenericContainer redis;
//
//	@Override
//	public void beforeAll(ExtensionContext context) {
//		redis = new GenericContainer(DockerImageName.parse(REDIS_IMAGE))
//			.withExposedPorts(REDIS_PORT);
//		redis.start();
//		System.setProperty("spring.redis.host", redis.getHost());
//		System.setProperty("spring.redis.port", String.valueOf(redis.getMappedPort(REDIS_PORT
//		)));
//	}
//}
