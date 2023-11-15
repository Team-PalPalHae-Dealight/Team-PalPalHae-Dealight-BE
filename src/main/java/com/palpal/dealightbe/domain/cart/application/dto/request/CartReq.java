package com.palpal.dealightbe.domain.cart.application.dto.request;

public record CartReq(
	Long itemId,
	int quantity
) {
}
