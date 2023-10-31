package com.palpal.dealightbe.domain.order.application;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.palpal.dealightbe.domain.order.application.dto.request.OrderCreateReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderProductReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderProductsReq;

class OrderServiceTest {

	@Test
	@DisplayName("신규 주문을 등록한다")
	void create() {
		// given
		Long memberProviderId = 1L;

		OrderCreateReq orderCreateReq = new OrderCreateReq(
			new OrderProductsReq(List.of(new OrderProductReq(1L, 3))),
			1L,
			"도착할 때까지 상품 냉장고에 보관 부탁드려요",
			LocalTime.of(12, 30),
			10000
		);

		// when

		// then
	}

}
