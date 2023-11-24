package com.palpal.dealightbe.domain.review.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderRepository;
import com.palpal.dealightbe.domain.order.domain.OrderStatus;
import com.palpal.dealightbe.domain.review.application.dto.request.ReviewCreateReq;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewCreateRes;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewRes;
import com.palpal.dealightbe.domain.review.domain.Review;
import com.palpal.dealightbe.domain.review.domain.ReviewContent;
import com.palpal.dealightbe.domain.review.domain.ReviewRepository;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.global.error.exception.BusinessException;

@Transactional
@SpringBootTest
class ReviewServiceIntegrationTest {
	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private ReviewService reviewService;

	@Nested
	@DisplayName("<리뷰 생성>")
	class createTest {
		@Nested
		@DisplayName("성공")
		class Success {

			@DisplayName("고객은 완료된 주문에 대해 리뷰를 작성할 수 있다.")
			@Test
			void createTest() {
				// given
				Member member = createMember();
				Store store = createStore();
				Order order = createOrder(member, store);

				long orderId = order.getId();

				ReviewCreateReq reviewCreateReq = new ReviewCreateReq(
					List.of(ReviewContent.Q1.getMessage(), ReviewContent.Q2.getMessage())
				);
				long memberProviderId = member.getProviderId();

				order.changeStatus(store.getMember(), "RECEIVED");
				order.changeStatus(store.getMember(), "COMPLETED");

				// when
				// then
				assertThat(order.getOrderStatus(), is(OrderStatus.COMPLETED));

				ReviewCreateRes reviewCreateRes = reviewService.create(orderId, reviewCreateReq,
					memberProviderId);

				assertThat(reviewCreateRes.ids(), hasSize(reviewCreateReq.messages().size()));

				List<String> messages = new ArrayList<>();
				reviewCreateRes.ids()
					.forEach(
						id -> {
							Review review = reviewRepository.findById(id).get();
							messages.add(review.getContent().getMessage());

							assertThat(review.getOrder().getId(), is(orderId));
						}
					);

				Assertions.assertThat(messages)
					.usingRecursiveComparison()
					.isEqualTo(reviewCreateReq.messages());

			}
		}

		@Nested
		@DisplayName("실패")
		class Fail {
			@DisplayName("주문을 한 고객 본인 외에는 리뷰를 작성할 수 없다.")
			@Test
			void createTest_fail_creator() {
				// given
				Member member = createMember();
				Store store = createStore();
				Order order = createOrder(member, store);

				long orderId = order.getId();
				long storeOwnerProviderId = store.getMember().getProviderId();

				ReviewCreateReq reviewCreateReq = new ReviewCreateReq(List.of("사장님이 친절해요", "가격이 저렴해요"));

				order.changeStatus(store.getMember(), "RECEIVED");
				order.changeStatus(store.getMember(), "COMPLETED");

				// when
				// then
				assertThat(order.getOrderStatus(), is(OrderStatus.COMPLETED));
				assertThrows(
					BusinessException.class,
					() ->
						reviewService.create(orderId, reviewCreateReq, storeOwnerProviderId)
				);
			}

			@DisplayName("주문이 완료되지 않은 경우 리뷰를 작성할 수 없다.")
			@Test
			void createTest_fail_status() {
				// given
				Member member = createMember();
				Store store = createStore();
				Order order = createOrder(member, store);

				long orderId = order.getId();
				long memberProviderId = member.getProviderId();

				ReviewCreateReq reviewCreateReq = new ReviewCreateReq(List.of("사장님이 친절해요", "가격이 저렴해요"));

				order.changeStatus(store.getMember(), "RECEIVED");

				// when
				// then
				assertThat(order.getOrderStatus(), is(not(OrderStatus.COMPLETED)));
				assertThrows(
					BusinessException.class,
					() ->
						reviewService.create(orderId, reviewCreateReq, memberProviderId)
				);
			}

			@DisplayName("이미 리뷰가 작성된 주문에 대해서 추가로 리뷰를 작성할 수 없다.")
			@Test
			void createTest_fail_already_exists() {
				// given
				Member member = createMember();
				Store store = createStore();
				Order order = createOrder(member, store);

				long orderId = order.getId();

				ReviewCreateReq reviewCreateReq = new ReviewCreateReq(
					List.of(ReviewContent.Q1.getMessage(), ReviewContent.Q2.getMessage())
				);
				long memberProviderId = member.getProviderId();

				order.changeStatus(store.getMember(), "RECEIVED");
				order.changeStatus(store.getMember(), "COMPLETED");

				// when
				// then

				assertDoesNotThrow(() -> reviewService.create(orderId, reviewCreateReq, memberProviderId));

				assertThrows(
					BusinessException.class,
					() -> {
						reviewService.create(orderId, reviewCreateReq, memberProviderId);
					}
				);
			}
		}
	}

	@Nested
	@DisplayName("<리뷰 조회>")
	class findTest {
		@Nested
		@DisplayName("성공")
		class Success {

			@DisplayName("고객은 각 주문에 대해 작성한 리뷰를 조회할 수 있다.")
			@Test
			void findByOrderId() {
				// given
				Member member = createMember();
				Store store = createStore();
				Order order = createOrder(member, store);

				List<Review> reviewReq = List.of(
					createReview(order, ReviewContent.Q1.getMessage()),
					createReview(order, ReviewContent.Q2.getMessage())
				);

				reviewRepository.saveAll(reviewReq);

				long orderId = order.getId();

				// when
				ReviewRes reviewRes = reviewService.findByOrderId(orderId, member.getProviderId());

				// then
				assertThat(reviewRes.messages(), hasSize(reviewReq.size()));

				Assertions.assertThat(reviewRes.messages())
					.usingRecursiveComparison()
					.isEqualTo(reviewReq.stream().map(m -> m.getContent().getMessage()).toList());

			}
		}

		@Nested
		@DisplayName("실패")
		class Fail {
			@DisplayName("리뷰 작성한 고객 외에 다른 고객이나 업체는 각 주문별 리뷰를 조회할 수 없다.")
			@Test
			void findByOrderId_by_others() {
				// given
				Member member = createMember();
				Store store = createStore();
				Order order = createOrder(member, store);

				List<Review> reviewReq = List.of(
					createReview(order, "사장님이 친절해요"),
					createReview(order, "가격이 저렴해요")
				);

				reviewRepository.saveAll(reviewReq);

				long orderId = order.getId();
				long storeOwnerId = store.getMember().getProviderId();

				// when
				// then
				assertThrows(
					BusinessException.class,
					() -> reviewService.findByOrderId(orderId, storeOwnerId)
				);
			}
		}
	}

	private Order createOrder(Member member, Store store) {
		Order order = Order.builder()
			.demand("도착할 때까지 상품 냉장고에 보관 부탁드려요")
			.arrivalTime(LocalTime.of(12, 30))
			.store(store)
			.member(member)
			.totalPrice(10000)
			.build();

		orderRepository.save(order);

		return order;

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

	private Review createReview(Order order, String content) {
		return Review.builder()
			.content(ReviewContent.messageOf(content))
			.order(order)
			.build();
	}

}
