package com.palpal.dealightbe.domain.item.domain;

import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ITEM;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@RedisHash("item:")
public class ItemRedisRepository {

	private static final String PREFIX = "item:";

	private final RedisTemplate<String, String> redisTemplate;
	private final ItemRepository itemRepository;

	public long save(Long key, Integer value) {
		redisTemplate
			.opsForValue()
			.set(generateKey(key), value.toString());

		return key;
	}

	public long update(Item item, int newStock) {
		long key = item.getId();

		return save(key, newStock);
	}

	private Item getItem(long key) {
		return itemRepository.findById(key)
			.orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_ITEM));
	}

	private String getStock(long key) {
		String stock = redisTemplate
			.opsForValue()
			.get(generateKey(key));

		if (stock == null) {
			Item item = getItem(key);

			save(item.getId(), item.getStock());
			stock = String.valueOf(item.getStock());
		}
		return stock;
	}

	private String generateKey(Long key) {
		return PREFIX + key.toString();
	}
}
