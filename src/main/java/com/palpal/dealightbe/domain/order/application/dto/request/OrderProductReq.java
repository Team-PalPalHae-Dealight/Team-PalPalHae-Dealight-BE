package com.palpal.dealightbe.domain.order.application.dto.request;

public record OrderProductReq(

	Long itemId,
	String name,
	int stock,
	int discountPrice,
	int originalPrice,
	String image
) {
}
