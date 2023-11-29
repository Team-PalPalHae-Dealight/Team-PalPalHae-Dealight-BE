package com.palpal.dealightbe.domain.order.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.palpal.dealightbe.config.JpaConfig;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;

@DataJpaTest
@Import(JpaConfig.class)
class OrderRepositoryTest {

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StoreRepository storeRepository;

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
			.openTime(LocalTime.of(9, 0))
			.closeTime(LocalTime.of(18, 0))
			.dayOff(Set.of(DayOff.SAT, DayOff.SUN))
			.build();

		memberRepository.save(member);
		memberRepository.save(storeOwner);
		storeRepository.save(store);
	}

	@Test
	@DisplayName("업체 아이디로 주문 목록을 조회할 수 있다 ")
	void findAllByStoreId() {
		// given
		long storeId = store.getId();

		String status = "RECEIVED";
		Pageable pageable = PageRequest.of(0, 2);

		Order order = createOrder(LocalTime.of(13, 0), 10000);
		Order order2 = createOrder(LocalTime.of(14, 0), 20000);
		Order order3 = createOrder(LocalTime.of(15, 0), 30000);

		orderRepository.saveAll(List.of(order, order2, order3));

		// when
		Slice<Order> result = orderRepository.findAllByStoreId(storeId, status, pageable);

		// then
		List<Order> orders = result.stream().toList();

		assertThat(result.getSize(), is(pageable.getPageSize()));

		Assertions.assertThat(orders.get(0))
			.usingRecursiveComparison()
			.isEqualTo(order);
		Assertions.assertThat(orders.get(1))
			.usingRecursiveComparison()
			.isEqualTo(order2);
	}

	@Test
	@DisplayName("고객 토큰으로 주문 목록을 조회할 수 있다")
	void findAllByMemberProviderId() {
		long memberProviderId = member.getId();

		String status = "RECEIVED";
		Pageable pageable = PageRequest.of(0, 2);

		Order order = createOrder(LocalTime.of(13, 0), 10000);
		Order order2 = createOrder(LocalTime.of(14, 0), 20000);
		Order order3 = createOrder(LocalTime.of(15, 0), 30000);

		orderRepository.saveAll(List.of(order, order2, order3));

		// when
		Slice<Order> result = orderRepository.findAllByStoreId(memberProviderId, status, pageable);

		// then
		List<Order> orders = result.stream().toList();

		assertThat(result.getSize(), is(pageable.getPageSize()));

		Assertions.assertThat(orders.get(0))
			.usingRecursiveComparison()
			.isEqualTo(order);

		Assertions.assertThat(orders.get(1))
			.usingRecursiveComparison()
			.isEqualTo(order2);
	}

	private Order createOrder(LocalTime arrivalTime, int totalPrice) {
		return Order.builder()
			.demand("도착할 때까지 상품 냉장고에 보관 부탁드려요")
			.arrivalTime(arrivalTime)
			.store(store)
			.member(member)
			.totalPrice(totalPrice)
			.build();
	}
}
