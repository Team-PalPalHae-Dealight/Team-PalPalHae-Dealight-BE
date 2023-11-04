package com.palpal.dealightbe.domain.order.application.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.palpal.dealightbe.domain.order.domain.Order;

public record OrdersRes(
	List<OrderRes> orders,
	boolean hasNext
) {

	public static OrdersRes from(Slice<Order> orders) {
		List<OrderRes> orderResList = orders.stream()
			.map(OrderRes::from)
			.toList();

		return new OrdersRes(orderResList, orders.hasNext());
	}
}
