package com.palpal.dealightbe.domain.order.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.error.exception.BusinessException;

class OrderTest {
	Member member;
	Member storeOwner;
	Store store;

	@BeforeEach
	void setUp() {
		member = Member.builder()
			.providerId(1L)
			.build();

		storeOwner = Member.builder()
			.providerId(1L)
			.build();

		store = Store.builder()
			.name("GS25")
			.storeNumber("12341432")
			.telephone("022341321")
			.openTime(LocalTime.of(3, 0))
			.closeTime(LocalTime.of(0, 0))
			.dayOff(Set.of(DayOff.SAT, DayOff.SUN))
			.build();
	}

	@DisplayName("고객 예정 도착 시간은 업체 영업 시간 이내이어야 한다")
	@Test
	void arrivalTimeTest(){
		// given
		// when
		// then
		assertThrows(BusinessException.class, () -> {
			LocalTime arrivalTime = LocalTime.of(1,0);
			new Order(member, store, arrivalTime, "도착할 때까지 상품 냉장고에 보관 부탁드려요", 10000);
		});
	}
}
