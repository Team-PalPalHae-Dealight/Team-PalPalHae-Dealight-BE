package com.palpal.dealightbe.domain.item.infrastructure;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ItemRedisRepository {

	private static final String PREFIX = "item:";

	private final RedisTemplate<String, String> redisTemplate;

	public Boolean lock(final Long key) {
		return redisTemplate
			.opsForValue()
			.setIfAbsent(generateKey(key), "lock", Duration.ofMillis(3_000));
	}

	public Boolean unlock(final Long key) {
		return redisTemplate.delete(generateKey(key));
	}

	private String generateKey(final Long key) {
		return PREFIX + key.toString();
	}
}
