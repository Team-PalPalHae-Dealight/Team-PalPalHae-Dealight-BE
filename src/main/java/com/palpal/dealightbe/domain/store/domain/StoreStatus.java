package com.palpal.dealightbe.domain.store.domain;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public enum StoreStatus {
	OPENED("영업 중"),
	CLOSED("영업 준비 중");

	private String name;

	@JsonValue
	public String getName() {
		return name;
	}

	@JsonCreator
	public static StoreStatus fromString(String text) {
		return Arrays.stream(StoreStatus.values())
			.filter(status -> status.name.equalsIgnoreCase(text) || status.toString().equalsIgnoreCase(text))
			.findFirst()
			.orElseGet(() -> {
				log.warn("PATCH:UPDATE:NOT_FOUND_STORE_STATUS : {}", text);
				throw new BusinessException(ErrorCode.NOT_FOUND_STATUS);
			});
	}
}
