package com.palpal.dealightbe.domain.cart.application.dto.response;

import java.util.List;

import com.palpal.dealightbe.domain.cart.domain.Cart;

public record CartsRes(
	List<CartRes> carts
) {

	public static CartsRes from(List<Cart> carts) {
		return new CartsRes(
			carts.stream()
				.map(CartRes::from)
				.toList()
		);
	}
}
