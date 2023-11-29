package com.palpal.dealightbe.domain.order.application.dto.response;

import java.util.List;

import com.palpal.dealightbe.domain.order.domain.Order;

public record OrderProductsRes(
	List<OrderProductRes> orderProducts
) {

	public static OrderProductsRes from(Order order) {
		List<OrderProductRes> orderProducts = order.getOrderItems()
			.stream()
			.map(orderItem -> OrderProductRes.of(orderItem.getItem(), orderItem.getQuantity()))
			.toList();

		return new OrderProductsRes(orderProducts);
	}
}
