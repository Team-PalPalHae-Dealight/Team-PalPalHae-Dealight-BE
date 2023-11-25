package com.palpal.dealightbe.domain.item.domain;

import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_ITEM_QUANTITY;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ITEM;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ItemRedisRepository {

	private static final String PREFIX = "item:";

	private final RedisTemplate<String, String> redisTemplate;
	private final ItemRepository itemRepository;

	public Boolean save(Long key, Integer value) {
		redisTemplate
			.opsForValue()
			.set(generateKey(key), value.toString());

		return Boolean.TRUE;
	}

	public Boolean update(long key, int quantity) {
		String stock = redisTemplate
			.opsForValue()
			.get(generateKey(key));

		if (stock == null) {
			throw new EntityNotFoundException(NOT_FOUND_ITEM);
		}

		int newStock = Integer.parseInt(stock) - quantity;
		if (newStock == 0) {
			Item item = itemRepository.findById(key)
				.orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_ITEM));

			item.updateStock(0);
			itemRepository.save(item);
		}

		if (newStock < 0) {
			throw new BusinessException(INVALID_ITEM_QUANTITY);
		}

		return save(key, newStock);
	}

	private String generateKey(Long key) {
		return PREFIX + key.toString();
	}
}
