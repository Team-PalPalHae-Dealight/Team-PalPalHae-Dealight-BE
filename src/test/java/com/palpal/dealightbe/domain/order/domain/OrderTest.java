package com.palpal.dealightbe.domain.order.domain;

import static com.palpal.dealightbe.domain.order.domain.OrderStatus.CANCELED;
import static com.palpal.dealightbe.domain.order.domain.OrderStatus.CONFIRMED;
import static com.palpal.dealightbe.domain.order.domain.OrderStatus.RECEIVED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.error.exception.BusinessException;

class OrderTest {
	Member member = createMember();
	Store store = createStore();

	static long providerId = 1111L;

	@DisplayName("고객 예정 도착 시간은 업체 영업 시간 이내이어야 한다")
	@Test
	void arrivalTimeTest() {
		// given
		// when
		// then
		assertThrows(BusinessException.class, () -> {
			LocalTime arrivalTime = LocalTime.of(1, 0);
			new Order(member, store, arrivalTime, "도착할 때까지 상품 냉장고에 보관 부탁드려요", 10000);
		});
	}

	@DisplayName("최종 금액이 입력된 값과 계산된 값이 다르면 예외가 발생한다")
	@Test
	void totalPriceTest() {
		// given
		Item item = createItem(store);
		int quantity = 2;
		Order order = createOrder(item.getDiscountPrice() * quantity * 2);

		List<OrderItem> orderItems = List.of(
			new OrderItem(order, item, quantity)
		);

		// when
		// then
		assertThrows(
			BusinessException.class,
			() -> order.addOrderItems(orderItems)
		);
	}

	@DisplayName("추가할 수 있는 상품 종류 수 이상으로 구매시 예외가 발생한다.")
	@Test
	void maxOrderItemsTest() {
		// given
		int MAX_ORDER_ITEMS = 5;
		Item item = createItem(store);
		Item item2 = createItem(store);
		Item item3 = createItem(store);
		Item item4 = createItem(store);
		Item item5 = createItem(store);
		Item item6 = createItem(store);

		int quantity = 6;
		Order order = createOrder(item.getDiscountPrice() * quantity);

		List<OrderItem> orderItems = List.of(
			new OrderItem(order, item, 1),
			new OrderItem(order, item2, 1),
			new OrderItem(order, item3, 1),
			new OrderItem(order, item4, 1),
			new OrderItem(order, item5, 1),
			new OrderItem(order, item6, 1)
		);

		// when
		// then
		assertThrows(
			BusinessException.class,
			() -> order.addOrderItems(orderItems)
		);
	}

	@DisplayName("[주문 상태 변경]")
	@Nested
	class updaterTest {
		@DisplayName("구매한 고객 본인이 아닌 다른 고객이면 예외가 발생한다")
		@Test
		void member() {
			// given
			Item item = createItem(store);
			Order order = createOrder(item.getDiscountPrice());
			Member updater = createMember();

			// when
			// then
			assertThat(updater, is(not(order.getMember())));

			assertThrows(
				BusinessException.class,
				() -> order.validateOrderUpdater(updater)
			);
		}

		@DisplayName("판매한 업체가 아닌 다른 업체면 예외가 발생한다")
		@Test
		void storeOwner() {
			// given
			Item item = createItem(store);
			Order order = createOrder(item.getDiscountPrice());
			Member updater = createMember();

			// when
			// then
			assertThat(updater, is(not(order.getStore().getMember())));

			assertThrows(
				BusinessException.class,
				() -> order.validateOrderUpdater(updater)
			);
		}

		@DisplayName("성공")
		@Test
		void changeStatus() {
			// given
			Item item = createItem(store);
			Order order = createOrder(item.getDiscountPrice());
			Member updater = order.getStore().getMember();

			// when
			// then
			assertThat(order.getOrderStatus(), is(RECEIVED));
			order.changeStatus(updater, "CONFIRMED");
			assertThat(order.getOrderStatus(), is(CONFIRMED));
		}

		@DisplayName("취소된 주문은 변경 불가능하다")
		@Test
		void canceledOrder() {
			// given
			Item item = createItem(store);
			Order order = createOrder(item.getDiscountPrice());
			Member updater = order.getStore().getMember();

			// when
			// then
			order.changeStatus(updater, "CANCELED");
			assertThat(order.getOrderStatus(), is(CANCELED));

			assertThrows(
				BusinessException.class,
				() -> order.changeStatus(updater, "CONFIRMED")
			);
		}

		@DisplayName("잘못된 주문 상태 입력시 예외가 발생한다")
		@Test
		void invalidOrderStatus() {
			// given
			Item item = createItem(store);
			Order order = createOrder(item.getDiscountPrice());
			Member updater = order.getStore().getMember();

			// when
			// then
			assertThrows(
				BusinessException.class,
				() -> order.changeStatus(updater, "INVALID_ORDER_STATUS")
			);
		}
	}

	private Item createItem(Store store) {
		return Item.builder()
			.name("떡볶이")
			.stock(1000)
			.originalPrice(4500)
			.discountPrice(4000)
			.description("기본 떡볶이 입니다.")
			.store(store)
			.build();
	}

	private Order createOrder(int totalPrice) {
		return Order.builder()
			.demand("도착할 때까지 상품 냉장고에 보관 부탁드려요")
			.arrivalTime(LocalTime.of(12, 30))
			.store(store)
			.member(member)
			.totalPrice(totalPrice)
			.build();
	}

	private Store createStore() {
		Member storeOwner = createMember();

		Store store = Store.builder()
			.name("GS25")
			.storeNumber("12341432")
			.telephone("022341321")
			.openTime(LocalTime.of(9, 0))
			.closeTime(LocalTime.of(18, 0))
			.dayOff(Set.of(DayOff.SAT, DayOff.SUN))
			.build();

		store.updateMember(storeOwner);

		return store;
	}

	private Member createMember() {
		return Member.builder()
			.nickName("본명")
			.realName("이름")
			.phoneNumber("010010101")
			.provider("kakao")
			.providerId(providerId++)
			.build();
	}

}
