package com.palpal.dealightbe.domain.order.application.dto.response;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.palpal.dealightbe.domain.order.domain.Order;

public record OrderRes(
	Long orderId,
	Long storeId,
	Long memberId,
	String memberNickName,
	String storeName,
	String demand,
	@JsonFormat(shape = STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
	LocalTime arrivalTime,
	OrderProductsRes orderProductsRes,
	int totalPrice,

	@JsonFormat(shape = STRING, pattern = "YYYY-MM-dd HH:mm", timezone = "Asia/Seoul")
	LocalDateTime createdAt,
	String status,

	boolean reviewContains
) {
	public static OrderRes from(Order order) {
		return new OrderRes(order.getId(), order.getStore().getId(), order.getMember().getId(),
			order.getMember().getNickName(), order.getStore().getName(),
			order.getDemand(), order.getArrivalTime(), OrderProductsRes.from(order), order.getTotalPrice(),
			order.getCreatedAt(), order.getOrderStatus().getText(), order.isReviewContains());
	}
}
