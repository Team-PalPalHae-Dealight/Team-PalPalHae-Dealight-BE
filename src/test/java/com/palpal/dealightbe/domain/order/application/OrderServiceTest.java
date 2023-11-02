package com.palpal.dealightbe.domain.order.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderStatusUpdateReq;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderStatusUpdateRes;
import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderRepository;
import com.palpal.dealightbe.domain.order.domain.OrderStatus;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.error.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
@Transactional
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;
	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private OrderService orderService;

	public static Member member;
	public static Member storeOwner;
	public static Store store;
	public static Order order;

	public static final long MEMBER_ID = 1234L;
	public static final long STORE_OWNER_ID = 1235L;

	@BeforeAll
	static void beforeAll() {
		member = Member.builder()
			.providerId(MEMBER_ID)
			.build();

		storeOwner = Member.builder()
			.providerId(STORE_OWNER_ID)
			.build();

		store = Store.builder()
			.name("GS25")
			.storeNumber("12341432")
			.telephone("022341321")
			.openTime(LocalTime.of(9, 0))
			.closeTime(LocalTime.of(18, 0))
			.dayOff(Set.of(DayOff.SAT, DayOff.SUN))
			.build();

		store.updateMember(storeOwner);

	}

	@Nested
	@DisplayName("<주문 상태 변경>")
	class UpdateStatusTest {
		@Nested
		@DisplayName("고객")
		class Member {
			@Test
			@DisplayName("'주문 확인' 상태에서 주문을 취소 할 수 있다")
			void member_statusUpdate_ReceivedToCanceled() {
				// given
				order = createOrder();

				when(memberRepository.findMemberByProviderId(MEMBER_ID))
					.thenReturn(Optional.of(member));

				when(orderRepository.findById(anyLong()))
					.thenReturn(Optional.of(order));

				OrderStatus changedStatus = OrderStatus.CANCELED;
				OrderStatusUpdateReq orderStatusUpdateReq = new OrderStatusUpdateReq(changedStatus.name());

				// when
				OrderStatusUpdateRes orderStatusUpdateRes
					= orderService.updateStatus(0L, orderStatusUpdateReq, MEMBER_ID);

				// then
				assertThat(orderStatusUpdateRes.status(), is(equalTo(changedStatus.name())));
				assertThat(order.getOrderStatus(), is(equalTo(changedStatus)));
			}

			@Test
			@DisplayName("'주문 접수' 상태 에서 주문을 취소 할 수 있다")
			void member_statusUpdate_ConfirmedToCanceled() {
				// given
				order = createOrder();

				when(memberRepository.findMemberByProviderId(MEMBER_ID))
					.thenReturn(Optional.of(member));

				when(memberRepository.findMemberByProviderId(STORE_OWNER_ID))
					.thenReturn(Optional.of(storeOwner));

				when(orderRepository.findById(anyLong()))
					.thenReturn(Optional.of(order));

				orderService.updateStatus(0L, new OrderStatusUpdateReq("CONFIRMED"), STORE_OWNER_ID);

				OrderStatus changedStatus = OrderStatus.CANCELED;
				OrderStatusUpdateReq orderStatusUpdateReq = new OrderStatusUpdateReq(changedStatus.name());

				// when
				// then
				assertThat(order.getOrderStatus(), is(equalTo(OrderStatus.CONFIRMED)));

				OrderStatusUpdateRes orderStatusUpdateRes
					= orderService.updateStatus(0L, orderStatusUpdateReq, MEMBER_ID);

				assertThat(orderStatusUpdateRes.status(), is(equalTo(changedStatus.name())));
				assertThat(order.getOrderStatus(), is(equalTo(changedStatus)));
			}

			@Test
			@DisplayName("'주문 확인' 상태 에서 '주문 접수'로 상태를 변경할 수 없다")
			void member_statusUpdateFailed_toConfirmed() {
				// given
				order = createOrder();

				when(orderRepository.findById(anyLong()))
					.thenReturn(Optional.of(order));
				when(memberRepository.findMemberByProviderId(MEMBER_ID))
					.thenReturn(Optional.of(member));

				OrderStatus changedStatus = OrderStatus.CONFIRMED;
				OrderStatusUpdateReq orderStatusUpdateReq = new OrderStatusUpdateReq(changedStatus.name());

				// when
				// then
				assertThrows(BusinessException.class, () -> {
					orderService.updateStatus(0L, orderStatusUpdateReq, MEMBER_ID);
				});
			}

			@Test
			@DisplayName("'주문 확인' 상태 에서 '주문 완료'로 상태를 변경할 수 없다")
			void member_statusUpdateFailed_toCompleted() {
				// given
				order = createOrder();

				when(orderRepository.findById(anyLong()))
					.thenReturn(Optional.of(order));
				when(memberRepository.findMemberByProviderId(MEMBER_ID))
					.thenReturn(Optional.of(member));

				OrderStatus changedStatus = OrderStatus.COMPLETED;
				OrderStatusUpdateReq orderStatusUpdateReq = new OrderStatusUpdateReq(changedStatus.name());

				// when
				// then
				assertThrows(BusinessException.class, () -> {
					orderService.updateStatus(0L, orderStatusUpdateReq, MEMBER_ID);
				});
			}
		}

		@Nested
		@DisplayName("업체")
		class Store {
			@Test
			@DisplayName("'주문 확인'에서 '주문 접수'로 상태를 변경 할 수 있다")
			void store_statusUpdate_receivedToConfirmed() {
				// given
				order = createOrder();

				when(memberRepository.findMemberByProviderId(STORE_OWNER_ID))
					.thenReturn(Optional.of(storeOwner));

				when(orderRepository.findById(anyLong()))
					.thenReturn(Optional.of(order));

				OrderStatus changedStatus = OrderStatus.CONFIRMED;
				OrderStatusUpdateReq orderStatusUpdateReq = new OrderStatusUpdateReq(changedStatus.name());

				// when
				OrderStatusUpdateRes orderStatusUpdateRes
					= orderService.updateStatus(0L, orderStatusUpdateReq, STORE_OWNER_ID);

				// then
				assertThat(orderStatusUpdateRes.status(), is(equalTo(changedStatus.name())));
				assertThat(order.getOrderStatus(), is(equalTo(changedStatus)));
			}

			@Test
			@DisplayName("'주문 접수'에서 '주문 완료'로 상태를 변경 할 수 있다")
			void store_statusUpdate_confirmedToCompleted() {
				// given

				order = createOrder();
				when(memberRepository.findMemberByProviderId(STORE_OWNER_ID))
					.thenReturn(Optional.of(storeOwner));

				when(orderRepository.findById(anyLong()))
					.thenReturn(Optional.of(order));

				orderService.updateStatus(0L, new OrderStatusUpdateReq("CONFIRMED"), STORE_OWNER_ID);

				OrderStatus changedStatus = OrderStatus.COMPLETED;
				OrderStatusUpdateReq orderStatusUpdateReq = new OrderStatusUpdateReq(changedStatus.name());

				// when
				// then
				assertThat(order.getOrderStatus(), is(equalTo(OrderStatus.CONFIRMED)));

				OrderStatusUpdateRes orderStatusUpdateRes
					= orderService.updateStatus(0L, orderStatusUpdateReq, STORE_OWNER_ID);

				assertThat(orderStatusUpdateRes.status(), is(equalTo(changedStatus.name())));
				assertThat(order.getOrderStatus(), is(equalTo(changedStatus)));
			}

			@Test
			@DisplayName("'주문 확인' 상태에서 주문을 취소할 수 있다")
			void store_statusUpdate_receivedToCanceled() {
				// given
				order = createOrder();

				when(memberRepository.findMemberByProviderId(STORE_OWNER_ID))
					.thenReturn(Optional.of(storeOwner));

				when(orderRepository.findById(anyLong()))
					.thenReturn(Optional.of(order));

				OrderStatus changedStatus = OrderStatus.CANCELED;
				OrderStatusUpdateReq orderStatusUpdateReq = new OrderStatusUpdateReq(changedStatus.name());

				// when
				OrderStatusUpdateRes orderStatusUpdateRes
					= orderService.updateStatus(0L, orderStatusUpdateReq, STORE_OWNER_ID);

				// then
				assertThat(orderStatusUpdateRes.status(), is(equalTo(changedStatus.name())));
				assertThat(order.getOrderStatus(), is(equalTo(changedStatus)));
			}

			@Test
			@DisplayName("'주문 접수' 상태에서 주문을 취소할 수 있다")
			void store_statusUpdate_confirmedToCanceled() {
				// given
				order = createOrder();

				when(memberRepository.findMemberByProviderId(STORE_OWNER_ID))
					.thenReturn(Optional.of(storeOwner));

				when(orderRepository.findById(anyLong()))
					.thenReturn(Optional.of(order));

				orderService.updateStatus(0L, new OrderStatusUpdateReq("CONFIRMED"), STORE_OWNER_ID);

				OrderStatus changedStatus = OrderStatus.CANCELED;
				OrderStatusUpdateReq orderStatusUpdateReq = new OrderStatusUpdateReq(changedStatus.name());

				// when
				// then
				assertThat(order.getOrderStatus(), is(equalTo(OrderStatus.CONFIRMED)));

				OrderStatusUpdateRes orderStatusUpdateRes
					= orderService.updateStatus(0L, orderStatusUpdateReq, STORE_OWNER_ID);

				assertThat(orderStatusUpdateRes.status(), is(equalTo(changedStatus.name())));
				assertThat(order.getOrderStatus(), is(equalTo(changedStatus)));
			}

			@Test
			@DisplayName("'주문 확인' 상태 에서 주문 접수 없이 바로 '주문 완료'로 상태를 변경할 수 없다")
			void store_statusUpdateFailed_miss() {
				// given
				order = createOrder();

				when(orderRepository.findById(anyLong()))
					.thenReturn(Optional.of(order));
				when(memberRepository.findMemberByProviderId(STORE_OWNER_ID))
					.thenReturn(Optional.of(storeOwner));

				OrderStatus changedStatus = OrderStatus.COMPLETED;
				OrderStatusUpdateReq orderStatusUpdateReq = new OrderStatusUpdateReq(changedStatus.name());

				// when
				// then
				assertThrows(BusinessException.class, () -> {
					orderService.updateStatus(0L, orderStatusUpdateReq, STORE_OWNER_ID);
				});
			}

			@Test
			@DisplayName("'주문 접수' 상태 에서 '주문 확인' 인 이전 상태로 되돌아 갈 수 없다")
			void store_statusUpdateFailed_undo() {
				// given
				order = createOrder();

				when(orderRepository.findById(anyLong()))
					.thenReturn(Optional.of(order));
				when(memberRepository.findMemberByProviderId(STORE_OWNER_ID))
					.thenReturn(Optional.of(storeOwner));

				orderService.updateStatus(0L, new OrderStatusUpdateReq("CONFIRMED"), STORE_OWNER_ID);

				OrderStatus changedStatus = OrderStatus.RECEIVED;
				OrderStatusUpdateReq orderStatusUpdateReq = new OrderStatusUpdateReq(changedStatus.name());

				// when
				// then

				assertThat(order.getOrderStatus(), is(equalTo(OrderStatus.CONFIRMED)));

				assertThrows(BusinessException.class, () -> {
					orderService.updateStatus(0L, orderStatusUpdateReq, STORE_OWNER_ID);
				});
			}
		}
	}

	private Order createOrder() {
		return Order.builder()
			.demand("도착할 때까지 상품 냉장고에 보관 부탁드려요")
			.arrivalTime(LocalTime.of(12, 30))
			.store(store)
			.member(member)
			.build();
	}

}
