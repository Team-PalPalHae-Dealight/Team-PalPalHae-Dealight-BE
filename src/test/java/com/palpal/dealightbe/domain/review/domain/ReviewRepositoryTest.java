package com.palpal.dealightbe.domain.review.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderRepository;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewStatistics;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewsRes;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;

@DataJpaTest
class ReviewRepositoryTest {

	@Autowired
	ReviewRepository reviewRepository;

	@Autowired
	StoreRepository storeRepository;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	private MemberRepository memberRepository;

	Member member = Member.builder()
		.providerId(1L)
		.build();

	Store store = Store.builder()
		.name("GS25")
		.storeNumber("12341432")
		.telephone("022341321")
		.openTime(LocalTime.of(9, 0))
		.closeTime(LocalTime.of(18, 0))
		.dayOff(Set.of(DayOff.SAT, DayOff.SUN))
		.build();

	Order order = Order.builder()
		.demand("도착할 때까지 상품 냉장고에 보관 부탁드려요")
		.arrivalTime(LocalTime.of(12, 30))
		.store(store)
		.member(member)
		.totalPrice(10000)
		.build();

	@BeforeEach
	void setUp() {
		storeRepository.save(store);
		memberRepository.save(member);
		orderRepository.save(order);
	}

	@Test
	@DisplayName("업체의 각 리뷰별로 개수를 조회할 수 있다")
	void selectStatisticsByStoreId() {
		// given

		long storeId = store.getId();

		List<Review> reviews = List.of(
			createReview("사장님이 친절해요"),
			createReview("사장님이 친절해요"),
			createReview("사장님이 친절해요"),
			createReview("가격이 저렴해요"),
			createReview("가격이 저렴해요")
		);

		reviewRepository.saveAll(reviews);

		// when

		List<ReviewStatistics> reviewStatistics = reviewRepository.selectStatisticsByStoreId(storeId);
		ReviewsRes reviewsRes = ReviewsRes.of(storeId, reviewStatistics);

		// then
		assertThat(reviewsRes.reviews()).hasSize(2)
			.extracting("content", "count")
			.containsExactlyInAnyOrder(
				tuple("사장님이 친절해요", 3),
				tuple("가격이 저렴해요", 2)
			);
	}

	private Review createReview(String content) {
		return Review.builder()
			.content(content)
			.order(order)
			.build();
	}
}
