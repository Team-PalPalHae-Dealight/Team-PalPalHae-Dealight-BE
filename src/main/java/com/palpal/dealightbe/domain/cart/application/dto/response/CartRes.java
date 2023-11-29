package com.palpal.dealightbe.domain.cart.application.dto.response;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.palpal.dealightbe.domain.cart.domain.Cart;

public record CartRes(
	Long cartId,
	Long itemId,
	Long storeId,
	Long memberProviderId,
	String itemName,
	int stock,
	int discountPrice,
	String itemImage,
	int quantity,
	String storeName,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
	LocalTime storeCloseTime,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime expirationDateTime
) {

	public static CartRes from(Cart cart) {

		return new CartRes(
			cart.getId(),
			cart.getItemId(),
			cart.getStoreId(),
			cart.getMemberProviderId(),
			cart.getItemName(),
			cart.getStock(),
			cart.getDiscountPrice(),
			cart.getItemImage(),
			cart.getQuantity(),
			cart.getStoreName(),
			cart.getStoreCloseTime(),
			cart.getExpirationDateTime()
		);
	}
}
