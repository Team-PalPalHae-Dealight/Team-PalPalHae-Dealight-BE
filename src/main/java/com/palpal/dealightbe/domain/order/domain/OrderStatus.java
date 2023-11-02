package com.palpal.dealightbe.domain.order.domain;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
	RECEIVED("주문 확인"),
	CONFIRMED("주문 접수"),
	COMPLETED("주문 완료"),
	CANCELED("주문 취소");

	private final String text;

	public static boolean isValidStatus(String status) {
		return Arrays.stream(OrderStatus.values())
			.anyMatch(orderStatus -> orderStatus.name().equals(status));
	}
}
