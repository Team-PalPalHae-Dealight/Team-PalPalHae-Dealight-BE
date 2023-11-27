package com.palpal.dealightbe.domain.item.domain;

import org.springframework.scheduling.annotation.Async;
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

	public void save(Item item, int quantity) {
		itemRedisRepository.update(item, quantity);
		updateItem(item, quantity);
	}

	@Async
	protected void updateItem(Item item, int quantity) {
		item.updateStock(item.getStock() - quantity);
	}
}
