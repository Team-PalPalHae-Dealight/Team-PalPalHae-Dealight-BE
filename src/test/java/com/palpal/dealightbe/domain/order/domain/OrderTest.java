package com.palpal.dealightbe.domain.order.domain;

import java.time.LocalTime;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Deprecated
class OrderTest {

	Address address;
	Member member;
	Store store;

	@BeforeEach
	void setUp() {
		address = Address.builder()
			.name("서울시 강남구")
			.xCoordinate(111.0)
			.yCoordinate(222.0)
			.build();
		member = Member.builder()
			.nickName("승원")
			.phoneNumber("01012344321")
			.provider("kakao")
			.providerId(12345678L)
			.realName("한승원")
			.build();

		store = Store.builder()
			.storeNumber("8888")
			.name("맛짱고기")
			.telephone("123123123")
			.address(address)
			.openTime(LocalTime.of(9, 0))
			.closeTime(LocalTime.of(23, 0))
			.dayOff(Set.of(DayOff.FRI))
			.build();
	}

	@DisplayName("요청 사항이 최대 100자 초과인 경우 예외가 발생한다.")
	@Test
	void demand() {
		// given
		String tooLongDemand = "100자가 넘는 요청사항을 고객이 작성한 경우 예외가 발생함을 확인하기 위한 예시 문장입니다. 현재 문장의 글자 수는 총 105글자 입니다. ABCDEFGHIJKLMNOPQRSTUVWXYZ";

		// when

		// then
		Assertions.assertThrows(
			BusinessException.class,
			() ->
				Order.builder()
					.demand(tooLongDemand)
					.arrivalTime(LocalTime.of(12, 30))
					.totalPrice(10000)
					.member(member)
					.store(store)
					.build());

	}
}
