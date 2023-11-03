package com.palpal.dealightbe.domain.item.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryCustom {

	Page<Item> findAllByStoreIdOrderByUpdatedAtDesc(Long storeId, Pageable pageable);
}
