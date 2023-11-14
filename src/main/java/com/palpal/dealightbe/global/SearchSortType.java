package com.palpal.dealightbe.global;

import java.util.Arrays;
import java.util.Objects;

import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public enum SearchSortType {
	DEADLINE("deadline"),
	DISCOUNT_RATE("discount-rate"),
	DISTANCE("distance"),
	;

	private final String type;

	SearchSortType(String type) {
		this.type = type;
	}

	public static SearchSortType findSortType(String sortType) {

		return Arrays.stream(SearchSortType.values())
			.filter(searchSortType -> Objects.equals(searchSortType.type, sortType))
			.findAny()
			.orElseThrow(() -> {
				log.error("INVALID_SEARCH_SORT_TYPE : {}", sortType);
				return new BusinessException(ErrorCode.INVALID_SEARCH_SORT_TYPE);
			});
	}
}
