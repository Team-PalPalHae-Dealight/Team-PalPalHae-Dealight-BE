package com.palpal.dealightbe.domain.order.application.dto.response;

import com.palpal.dealightbe.domain.item.domain.Item;

public record OrderProductRes(
	long itemId,
	String name,
	int stock,
	int discountPrice,
	int originalPrice,
	String image
) {

	public static OrderProductRes from(Item item) {
		return new OrderProductRes(
			item.getId(),
			item.getName(),
			item.getStock(),
			item.getDiscountPrice(),
			item.getOriginalPrice(),
			item.getImage()
		);
	}
}
