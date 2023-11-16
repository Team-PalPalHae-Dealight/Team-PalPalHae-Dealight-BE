package com.palpal.dealightbe.global;

import java.util.Arrays;
import java.util.Objects;

import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public enum ListSortType {
	DEADLINE("deadline"),
	DISCOUNT_RATE("discount-rate"),
	DISTANCE("distance"),
	;

	private final String type;

	ListSortType(String type) {
		this.type = type;
	}

	public static ListSortType findSortType(String sortType) {

		return Arrays.stream(ListSortType.values())
			.filter(listSortType -> Objects.equals(listSortType.type, sortType))
			.findAny()
			.orElseThrow(() -> {
				log.error("INVALID_LIST_SORT_TYPE : {}", sortType);
				return new BusinessException(ErrorCode.INVALID_LIST_SORT_TYPE);
			});
	}
}
