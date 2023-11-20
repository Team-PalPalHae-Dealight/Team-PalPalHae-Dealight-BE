package com.palpal.dealightbe.global;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@AllArgsConstructor
public enum ListSortType {
	DEADLINE("deadline"),
	DISCOUNT_RATE("discount-rate"),
	DISTANCE("distance"),
	;

	private final String type;

	@JsonValue
	public String getType() {
		return type;
	}

	@JsonCreator
	public static ListSortType findSortType(String sortType) {

		return Arrays.stream(ListSortType.values())
			.filter(listSortType -> listSortType.type.equalsIgnoreCase(sortType) || listSortType.toString().equalsIgnoreCase(sortType))
			.findFirst()
			.orElseThrow(() -> {
				log.error("INVALID_SEARCH_SORT_TYPE : {}", sortType);
				return new BusinessException(ErrorCode.INVALID_SEARCH_SORT_TYPE);
			});
	}
}
