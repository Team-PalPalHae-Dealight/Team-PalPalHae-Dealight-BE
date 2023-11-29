package com.palpal.dealightbe.domain.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
	ORDER_CONFIRMED("주문 확인"),
	ORDER_RECEIVED("주문 접수"),
	ORDER_COMPLETED("주문 완료"),
	ORDER_CANCELED("주문 취소");

	private final String description;
}
