package com.palpal.dealightbe.domain.order.application.dto.response;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.palpal.dealightbe.domain.order.domain.Order;

public record OrderRes(

	Long orderId,
	Long storeId,
	Long memberId,
	String storeName,
	String demand,
	LocalTime arrivalTime,
	OrderProductsRes orderProductsRes,
	int totalPrice,
	LocalDateTime createdAt,
	String status
) {
	public static OrderRes from(Order order) {
		return new OrderRes(order.getId(), order.getStore().getId(), order.getMember().getId(),
			order.getStore().getName(),
			order.getDemand(), order.getArrivalTime(), OrderProductsRes.from(order), order.getTotalPrice(),
			order.getCreatedAt(),
			order.getOrderStatus().getText());
	}
}
