package com.palpal.dealightbe.domain.cart.application.dto.request;

import java.util.List;

public record CartsReq(
	List<CartReq> carts
) {
}
