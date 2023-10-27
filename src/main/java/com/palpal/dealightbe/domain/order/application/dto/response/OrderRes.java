package com.palpal.dealightbe.domain.order.application.dto.response;

import java.time.LocalDateTime;

import com.palpal.dealightbe.domain.order.application.dto.request.OrderProductsReq;
import com.palpal.dealightbe.domain.order.domain.Order;

public class OrderRes(

	Long orderId,
	Long storeId,
	Long memberId,
	String storeName,
	String demand,
	LocalDateTime arrivalTime,
	OrderProductsReq orderProductsReq,
	int totalPrice,
	String createdAt,
	String status
) {
	public static OrderRes of(Order order) {
		new OrderRes(order.getId(), order.getStore().getId(), order.getMember().getId(), order.getStore().getName(),
			order.getDemand(), order.getArrivalTime(), orderProductsReq, totalPrice, order.getCreatedAt(),
			order.getOrderStatus());
	}
}
