package com.palpal.dealightbe.domain.order.application.dto.request;

import java.time.LocalTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.store.domain.Store;

public record OrderCreateReq(
	@NotNull(message = "주문할 상품과 수량을 입력해 주세요")
	OrderProductsReq orderProductsReq,

	@NotNull(message = "상품을 주문한 업체의 아이디를 입력해 주세요")
	Long storeId,

	String demand,

	@NotNull(message = "도착 예정 시간을 입력해 주세요")
	LocalTime arrivalTime,

	@Positive(message = "1개 이상의 상품을 주문해 주세요")
	int totalPrice
) {

	public static Order toOrder(OrderCreateReq orderCreateReq, Member member, Store store) {
		return Order.builder()
			.member(member)
			.store(store)
			.arrivalTime(orderCreateReq.arrivalTime)
			.demand(orderCreateReq.demand)
			.build();
	}
}
