package com.palpal.dealightbe.domain.cart.domain;

import java.util.Arrays;
import java.util.Objects;

import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum CartAdditionType {
	BY_CHECK("check"),
	BY_CLEAR("clear");

	private final String type;

	CartAdditionType(String type) {
		this.type = type;
	}

	public static CartAdditionType findCartAdditionType(String additionType) {

		return Arrays.stream(CartAdditionType.values())
			.filter(cartAdditionType -> Objects.equals(cartAdditionType.type, additionType))
			.findAny()
			.orElseThrow(() -> {
				log.error("INVALID_CART_ADDITION_TYPE : {}", additionType);
				return new BusinessException(ErrorCode.INVALID_CART_ADDITION_TYPE);
			});
	}
}
