package com.palpal.dealightbe.domain.item.domain;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ItemJpaRedisRepository {

	private final ItemRepository itemRepository;
	private final ItemRedisRepository itemRedisRepository;

	public Item save(Item item) {
		itemRepository.save(item);
		itemRedisRepository.save(item.getId(), item.getStock());

		return item;
	}

	public void save(long itemId, int quantity) {
		itemRedisRepository.update(itemId, quantity);
	}
}
