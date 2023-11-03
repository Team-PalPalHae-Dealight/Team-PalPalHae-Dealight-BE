package com.palpal.dealightbe.domain.order.application.dto.response;

import com.palpal.dealightbe.domain.order.domain.Order;

public record OrderStatusUpdateRes(
	Long orderId,
	String status
) {

	public static OrderStatusUpdateRes from(Order order) {
		Long orderId = order.getId();
		return new OrderStatusUpdateRes(orderId, order.getOrderStatus().name());
	}
}
