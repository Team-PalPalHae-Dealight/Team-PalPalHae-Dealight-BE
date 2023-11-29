package com.palpal.dealightbe.domain.order.application.dto.request;

import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderItem;

public record OrderProductReq(

	long itemId,
	int quantity
) {
	public static OrderItem toOrderItem(Item item, Order order, OrderProductReq request) {
		return OrderItem.builder()
			.item(item)
			.order(order)
			.quantity(request.quantity)
			.build();
	}
}
