package com.palpal.dealightbe.domain.item.application.dto.response;

import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreInfoRes;

public record ItemRes(
	Long itemId,
	Long storeId,
	String name,
	int stock,
	int discountPrice,
	int originalPrice,
	String description,
	String information,
	String image,
	StoreInfoRes storeInfoRes,
	AddressRes storeAddressRes
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
			item.getInformation(),
			item.getImage(),
			StoreInfoRes.from(item.getStore()),
			AddressRes.from(item.getStore().getAddress())
		);
	}
}
