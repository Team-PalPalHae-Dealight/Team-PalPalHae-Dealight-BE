package com.palpal.dealightbe.domain.order.application.dto.request;

public record OrderProductReq(

	long itemId,
	int quantity
) {
}
