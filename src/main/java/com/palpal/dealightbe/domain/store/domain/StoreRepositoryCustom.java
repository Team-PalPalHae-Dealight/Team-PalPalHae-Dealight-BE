package com.palpal.dealightbe.domain.store.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface StoreRepositoryCustom {

	Slice<Store> findByKeywordAndDistanceWithin3KmAndSortCondition(
		double xCoordinate,
		double yCoordinate,
		String keyword,
		String sortBy,
		Long cursor,
		Pageable pageable);
}
