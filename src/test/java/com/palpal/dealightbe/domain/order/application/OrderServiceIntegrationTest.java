package com.palpal.dealightbe.domain.order.application;

import static com.palpal.dealightbe.domain.order.domain.OrderStatus.CANCELED;
import static com.palpal.dealightbe.domain.order.domain.OrderStatus.RECEIVED;
import static com.palpal.dealightbe.domain.store.domain.StoreStatus.OPENED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.palpal.dealightbe.common.IntegrationTest;
import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.notification.application.NotificationService;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderCreateReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderProductReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderProductsReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderStatusUpdateReq;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderRes;
import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderItem;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.error.exception.BusinessException;

public class OrderServiceIntegrationTest extends IntegrationTest {

	@Nested
	@DisplayName("[주문 생성]")
	class createTest {
		@Nested
		@DisplayName("성공")
		class Success {

			@DisplayName("고객은 상품을 주문할 수 있다.")
			@Test
			void member_create() {
				// given
				Store store = createStore();
				Item item = createItem(store);
				Member member = createMember();

				int originalStock = item.getStock();
				long itemId = item.getId();
				int quantity = 2;

				OrderCreateReq orderCreateReq = new OrderCreateReq(
					new OrderProductsReq(
						List.of(new OrderProductReq(item.getId(), quantity))
					),
					store.getId(), "도착할 때까지 상품 냉장고에 보관 부탁드려요",
					LocalTime.of(12, 30), item.getDiscountPrice() * 2
				);

				// when
				OrderRes orderRes = orderService.create(orderCreateReq, member.getProviderId());
				item = itemRepository.findById(itemId).get();

				// then
				long orderId = orderRes.orderId();
				Order createdOrder = orderRepository.findById(orderId).get();
				itemRepository.flush();

				assertThat(createdOrder.getStore().getId(), is(store.getId()));
				assertThat(createdOrder.getMember().getId(), is(member.getId()));
				assertThat(createdOrder.getStore().getName(), is(store.getName()));
				assertThat(createdOrder.getOrderStatus(), is(RECEIVED));

				assertThat(createdOrder.getDemand(), is(orderCreateReq.demand()));
				assertThat(createdOrder.getArrivalTime(), is(orderCreateReq.arrivalTime()));
				assertThat(createdOrder.getTotalPrice(), is(orderCreateReq.totalPrice()));

				OrderItem orderedItem = createdOrder.getOrderItems().get(0);
				assertThat(orderedItem.getItem().getId(), is(item.getId()));
				assertThat(item.getStock(), is(originalStock - quantity));
			}
		}

		@Nested
		@DisplayName("실패")
		class Fail {
			@DisplayName("재고보다 많은 수량을 구매 시도하면 예외가 발생한다.")
			@Test
			void deductStock() {
				// given
				Store store = createStore();

				Item item = createItem(store);
				Item item2 = createItem(store);
				int totalPrice = item.getDiscountPrice() * 2;

				Member member = createMember();
				Long memberProviderId = member.getProviderId();

				OrderCreateReq orderCreateReq = new OrderCreateReq(
					new OrderProductsReq(
						List.of(
							new OrderProductReq(item.getId(), item.getStock() + 1),
							new OrderProductReq(item2.getId(), item2.getStock() - 1)
						)
					), store.getId(), "도착할 때까지 상품 냉장고에 보관 부탁드려요", LocalTime.of(12, 30), totalPrice
				);

				// when
				// then
				assertThrows(BusinessException.class,
					() -> orderService.create(orderCreateReq, memberProviderId)
				);
			}

			@DisplayName("한번에 5개 종류 이상의 상품을 구매 시도하면 예외가 발생한다.")
			@Test
			void maxOrderItems() {
				// given
				Store store = createStore();

				Item item = createItem(store);
				Item item2 = createItem(store);
				Item item3 = createItem(store);
				Item item4 = createItem(store);
				Item item5 = createItem(store);
				Item item6 = createItem(store);

				int totalPrice = item.getDiscountPrice() * 6;

				Member member = createMember();
				Long memberProviderId = member.getProviderId();

				OrderCreateReq orderCreateReq = new OrderCreateReq(
					new OrderProductsReq(
						List.of(
							new OrderProductReq(item.getId(), item.getStock() + 1),
							new OrderProductReq(item2.getId(), item2.getStock() - 1),
							new OrderProductReq(item3.getId(), item3.getStock() - 1),
							new OrderProductReq(item4.getId(), item4.getStock() - 1),
							new OrderProductReq(item5.getId(), item5.getStock() - 1),
							new OrderProductReq(item6.getId(), item6.getStock() - 1)
						)
					), store.getId(), "도착할 때까지 상품 냉장고에 보관 부탁드려요", LocalTime.of(12, 30), totalPrice
				);

				// when
				// then
				assertThrows(BusinessException.class,
					() -> orderService.create(orderCreateReq, memberProviderId)
				);
			}

			@DisplayName("입력된 총 금액이 실제와 일치하지 않는 경우 예외가 발생한다.")
			@Test
			void validateTotalPrice() {
				// given
				Store store = createStore();

				Item item = createItem(store);
				Item item2 = createItem(store);

				int totalPrice = 0;

				Member member = createMember();
				Long memberProviderId = member.getProviderId();

				OrderCreateReq orderCreateReq = new OrderCreateReq(
					new OrderProductsReq(
						List.of(
							new OrderProductReq(item.getId(), item.getStock() - 1),
							new OrderProductReq(item2.getId(), item2.getStock() - 1)
						)
					), store.getId(), "도착할 때까지 상품 냉장고에 보관 부탁드려요", LocalTime.of(12, 30), totalPrice
				);

				// when
				// then
				assertThrows(BusinessException.class,
					() -> orderService.create(orderCreateReq, memberProviderId)
				);
			}
		}
	}

	@Nested
	@DisplayName("[주문 상태 변경]")
	class updateStatusTest {
		@Nested
		@DisplayName("성공")
		class Success {

			@DisplayName("고객이 주문한 상품을 취소하면 재고가 다시 늘어난다.")
			@Test
			void cancel_order() {
				// given
				Store store = createStore();
				Item item = createItem(store);
				Member member = createMember();

				int originalStock = item.getStock();
				int quantity = 2;
				long itemId = item.getId();

				OrderCreateReq orderCreateReq = new OrderCreateReq(
					new OrderProductsReq(
						List.of(new OrderProductReq(itemId, quantity))
					),
					store.getId(), "도착할 때까지 상품 냉장고에 보관 부탁드려요",
					LocalTime.of(12, 30), item.getDiscountPrice() * quantity
				);

				OrderRes orderRes = orderService.create(orderCreateReq, member.getProviderId());

				long orderId = orderRes.orderId();
				Order order = orderRepository.findById(orderId).get();

				item = itemRepository.findById(itemId).get();

				// when
				// then

				assertThat(item.getStock(), is(originalStock - quantity));

				orderService.updateStatus(orderId, new OrderStatusUpdateReq("CANCELED"), member.getProviderId());
				assertThat(order.getOrderStatus(), is(CANCELED));
				assertThat(item.getStock(), is(originalStock));
			}
		}
	}

	@DisplayName("[주문 단건 조회]")
	@Nested
	class findById {
		@DisplayName("성공")
		@Test
		void success() {
			// given
			Store store = createStore();
			Item item = createItem(store);
			Member member = createMember();

			int quantity = 2;

			OrderCreateReq orderCreateReq = new OrderCreateReq(
				new OrderProductsReq(
					List.of(new OrderProductReq(item.getId(), quantity))
				),
				store.getId(), "도착할 때까지 상품 냉장고에 보관 부탁드려요",
				LocalTime.of(12, 30), item.getDiscountPrice() * 2
			);

			// when
			// then
			OrderRes orderRes = orderService.create(orderCreateReq, member.getProviderId());
			Order order = orderRepository.findById(orderRes.orderId()).get();
			Long actualItemId = order.getOrderItems().get(0).getItem().getId();
			long orderedItemId = orderCreateReq.orderProductsReq().orderProducts().get(0).itemId();

			assertThat(order.getId(), is(orderRes.orderId()));
			assertThat(actualItemId, is(orderedItemId));
		}
	}

	private Address createAddress() {

		return Address.builder()
			.xCoordinate(127.0324773)
			.yCoordinate(37.5893876)
			.build();
	}

	private Store createStore() {
		Member storeOwner = createMember();
		Address address = createAddress();

		Store store = Store.builder()
			.name("GS25")
			.storeNumber("12341432")
			.telephone("022341321")
			.openTime(LocalTime.of(9, 0))
			.closeTime(LocalTime.of(18, 0))
			.dayOff(Set.of(DayOff.SAT, DayOff.SUN))
			.address(address)
			.build();

		storeRepository.save(store);

		store.updateMember(storeOwner);
		store.updateStatus(OPENED);

		return store;
	}

	private Member createMember() {
		Member member = Member.builder()
			.providerId(memberRepository.count() + 1)
			.build();

		memberRepository.save(member);

		return member;
	}

	private Item createItem(Store store) {
		Item item = Item.builder()
			.name("떡볶이")
			.stock(3)
			.originalPrice(4500)
			.discountPrice(4000)
			.description("기본 떡볶이 입니다.")
			.store(store)
			.build();

		itemRepository.save(item);

		return item;
	}
}
