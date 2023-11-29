package com.palpal.dealightbe.domain.item.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ItemRepositoryCustom {

	Slice<Item> findAllByStoreIdOrderByUpdatedAtDesc(Long storeId, Pageable pageable);

	Slice<Item> findAllByOpenedStatusAndDistanceWithin3KmAndSortCondition(double xCoordinate, double yCoordinate,
		String sortBy, Pageable pageable);
}
