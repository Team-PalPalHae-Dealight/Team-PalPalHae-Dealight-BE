package com.palpal.dealightbe.domain.item.application.dto.response;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.item.domain.Item;

public record ItemRes(
	Long itemId,
	Long storeId,
	String itemName,
	int stock,
	int discountPrice,
	int originalPrice,
	String description,
	String image,
	String storeName,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
	LocalTime storeCloseTime,

	AddressRes storeAddress
) {

	public static ItemRes from(Item item) {

		return new ItemRes(
			item.getId(),
			item.getStore().getId(),
			item.getName(),
			item.getStock(),
			item.getDiscountPrice(),
			item.getOriginalPrice(),
			item.getDescription(),
			item.getImage(),
			item.getStore().getName(),
			item.getStore().getCloseTime(),
			AddressRes.from(item.getStore().getAddress())
		);
	}
}
