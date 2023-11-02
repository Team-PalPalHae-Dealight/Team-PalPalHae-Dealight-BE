package com.palpal.dealightbe.domain.item.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

	boolean existsByNameAndStoreId(String name, Long storeId);

	List<Item> findAllByStoreId(Long storeId);
}
