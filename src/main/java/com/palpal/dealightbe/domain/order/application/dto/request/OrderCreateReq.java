package com.palpal.dealightbe.domain.order.application.dto.request;

public record OrderCreateReq(

	OrderProductsReq orderProductsReq,
	Long storeId,
	String demand,
	String arrivalTime,
	int totalPrice
) {
}
