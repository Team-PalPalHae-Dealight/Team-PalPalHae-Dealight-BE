package com.palpal.dealightbe.domain.order.application;

import static com.palpal.dealightbe.domain.order.domain.OrderStatus.RECEIVED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.item.domain.ItemRepository;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderCreateReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderProductReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderProductsReq;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderRes;
import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderItem;
import com.palpal.dealightbe.domain.order.domain.OrderRepository;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.global.error.exception.BusinessException;

@Transactional
@SpringBootTest
public class OrderServiceIntegrationTest {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private OrderService orderService;
	@Autowired
	private ItemRepository itemRepository;

	@Nested
	@DisplayName("주문 생성")
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

				OrderCreateReq orderCreateReq = new OrderCreateReq(
					new OrderProductsReq(
						List.of(new OrderProductReq(item.getId(), 2))
					),
					store.getId(), "도착할 때까지 상품 냉장고에 보관 부탁드려요", LocalTime.of(12, 30), item.getDiscountPrice()
				);

				// when
				OrderRes orderRes = orderService.create(orderCreateReq, member.getProviderId());

				// then
				long orderId = orderRes.orderId();
				Order createdOrder = orderRepository.findById(orderId).get();

				assertThat(createdOrder.getStore().getId(), is(store.getId()));
				assertThat(createdOrder.getMember().getId(), is(member.getId()));
				assertThat(createdOrder.getStore().getName(), is(store.getName()));
				assertThat(createdOrder.getOrderStatus().getText(), is(RECEIVED.getText()));

				assertThat(createdOrder.getDemand(), is(createdOrder.getDemand()));
				assertThat(createdOrder.getArrivalTime(), is(createdOrder.getArrivalTime()));
				assertThat(createdOrder.getTotalPrice(), is(createdOrder.getTotalPrice()));

				OrderItem orderedItem = createdOrder.getOrderItems().get(0);
				assertThat(orderedItem.getItem(), is(item));
				assertThat(orderedItem.getQuantity(),
					is(orderCreateReq.orderProductsReq().orderProducts().get(0).quantity()));

				OrderItem orderItem = createdOrder.getOrderItems().get(0);
				assertThat(orderItem.getOrder(), is(createdOrder));
			}
		}

		@Nested
		@DisplayName("실패")
		class Fail {
			@DisplayName("재고보다 많은 수량을 구매 시도 하면 예외가 발생한다.")
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

			@DisplayName("한번에 5개 종류 이상의 상품을 구매 시도 하면 예외가 발생한다.")
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
		}
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
		storeRepository.save(store);

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
			.information("통신사 할인 불가능 합니다.")
			.store(store)
			.build();

		itemRepository.save(item);

		return item;
	}
}
