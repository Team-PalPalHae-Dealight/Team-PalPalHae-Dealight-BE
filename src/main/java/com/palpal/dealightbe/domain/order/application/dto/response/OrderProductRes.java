package com.palpal.dealightbe.domain.order.application.dto.response;

import com.palpal.dealightbe.domain.item.domain.Item;

public record OrderProductRes(
	long itemId,
	String name,
	int quantity,
	int discountPrice,
	int originalPrice,
	String image
) {

	public static OrderProductRes of(Item item, int quantity) {
		return new OrderProductRes(
			item.getId(),
			item.getName(),
			quantity,
			item.getDiscountPrice(),
			item.getOriginalPrice(),
			item.getImage()
		);
	}
}
