package com.palpal.dealightbe.domain.item.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ItemRepositoryCustom {

	Slice<Item> findAllByStoreIdOrderByUpdatedAtDesc(Long storeId, Pageable pageable);
}
