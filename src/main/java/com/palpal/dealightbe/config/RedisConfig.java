package com.palpal.dealightbe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.domain.notification.application.dto.response.NotificationRes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableRedisRepositories
public class RedisConfig {

	@Value("${spring.redis.host}")
	private String redisHost;

	@Value("${spring.redis.port}")
	private int redisPort;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {

		return new LettuceConnectionFactory(redisHost, redisPort);
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate() {
		StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
		stringRedisTemplate.setConnectionFactory(redisConnectionFactory());

		stringRedisTemplate.setEnableTransactionSupport(true);

		return stringRedisTemplate;
	}

	@Bean
	public RedisOperations<String, NotificationRes> eventRedisOperations(
		RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
		final Jackson2JsonRedisSerializer<NotificationRes> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
			NotificationRes.class);
		jsonRedisSerializer.setObjectMapper(objectMapper);
		final RedisTemplate<String, NotificationRes> eventRedisTemplate = new RedisTemplate<>();
		eventRedisTemplate.setConnectionFactory(redisConnectionFactory);
		eventRedisTemplate.setKeySerializer(RedisSerializer.string());
		eventRedisTemplate.setValueSerializer(jsonRedisSerializer);
		eventRedisTemplate.setHashKeySerializer(RedisSerializer.string());
		eventRedisTemplate.setHashValueSerializer(jsonRedisSerializer);
		return eventRedisTemplate;
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
		final RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
		redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
		redisMessageListenerContainer.setErrorHandler(
			e -> log.error("There was an error in redis message listener container", e));
		return redisMessageListenerContainer;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		return new JpaTransactionManager();
	}
}
