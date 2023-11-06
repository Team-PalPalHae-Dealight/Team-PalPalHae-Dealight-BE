package com.palpal.dealightbe.domain.item.domain;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Objects;

import com.palpal.dealightbe.global.error.exception.BusinessException;

import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_ITEM_SORT_TYPE;

@Slf4j
public enum ItemSortType {
	DEADLINE("deadline"),
	DISCOUNT_RATE("discount-rate"),
	DISTANCE("distance");

	private final String type;

	ItemSortType(String type) {
		this.type = type;
	}

	public static ItemSortType findItemSortType(String sortType) {

		return Arrays.stream(ItemSortType.values())
			.filter(itemSortType -> Objects.equals(itemSortType.type, sortType))
			.findAny()
			.orElseThrow(() -> {
				log.error("INVALID_ITEM_SORT_TYPE : {}", sortType);
				return new BusinessException(INVALID_ITEM_SORT_TYPE);
			});
	}
}
