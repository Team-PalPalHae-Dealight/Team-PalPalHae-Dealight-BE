package com.palpal.dealightbe.domain.order.domain;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
	RECEIVED("주문 접수", "새 주문이 도착했습니다: %d"),
	CONFIRMED("주문 확인", "주문이 수락되었습니다: %d"),
	COMPLETED("주문 완료", "주문이 완료되었습니다: %d"),
	CANCELED("주문 취소", "주문이 취소되었습니다: %d");

	private final String text;
	private final String messageTemplate;

	public static boolean isValidStatus(String status) {
		return Arrays.stream(OrderStatus.values())
			.anyMatch(orderStatus -> orderStatus.name().equals(status));
	}

	public String createMessage(Long orderId) {
		return String.format(this.messageTemplate, orderId);
	}
}
