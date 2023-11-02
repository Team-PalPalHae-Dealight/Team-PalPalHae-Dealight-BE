package com.palpal.dealightbe.domain.item.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

	boolean existsByNameAndStoreId(String name, Long storeId);
}
