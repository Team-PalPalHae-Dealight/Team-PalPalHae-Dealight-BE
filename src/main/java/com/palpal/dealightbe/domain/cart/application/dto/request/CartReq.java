package com.palpal.dealightbe.domain.cart.application.dto.request;

import com.palpal.dealightbe.domain.cart.domain.Cart;
import com.palpal.dealightbe.domain.item.domain.Item;

public record CartReq(
	Long itemId,
	int quantity
) {

	public static Cart toCart(Long memberProviderId, Item item) {

		return Cart.builder()
			.itemId(item.getId())
			.storeId(item.getStore().getId())
			.memberProviderId(memberProviderId)
			.itemName(item.getName())
			.stock(item.getStock())
			.discountPrice(item.getDiscountPrice())
			.itemImage(item.getImage())
			.storeName(item.getStore().getName())
			.storeCloseTime(item.getStore().getCloseTime())
			.build();
	}
}
