package com.palpal.dealightbe.domain.item.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {

	boolean existsByNameAndStoreId(String name, Long storeId);
}
