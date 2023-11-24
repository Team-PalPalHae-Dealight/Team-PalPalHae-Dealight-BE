package com.palpal.dealightbe.domain.review.application;

import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ORDER;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_STORE;
import static com.palpal.dealightbe.global.error.ErrorCode.UNAUTHORIZED_REQUEST;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderRepository;
import com.palpal.dealightbe.domain.review.application.dto.request.ReviewCreateReq;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewCreateRes;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewRes;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewStatistics;
import com.palpal.dealightbe.domain.review.application.dto.response.StoreReviewsRes;
import com.palpal.dealightbe.domain.review.domain.Review;
import com.palpal.dealightbe.domain.review.domain.ReviewRepository;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

	private final StoreRepository storeRepository;
	private final OrderRepository orderRepository;
	private final ReviewRepository reviewRepository;

	public ReviewCreateRes create(Long orderId, ReviewCreateReq request, Long memberProviderId) {
		Order order = getOrder(orderId);

		checkMemberAuthority(memberProviderId, order);

		List<Review> reviews = ReviewCreateReq.toReviews(request, order);
		reviewRepository.saveAll(reviews);
		order.createReviews();

		return ReviewCreateRes.from(reviews);
	}

	@Transactional(readOnly = true)
	public StoreReviewsRes findByStoreId(Long id) {
		List<ReviewStatistics> reviews = reviewRepository.selectStatisticsByStoreId(id);

		return StoreReviewsRes.of(id, reviews);
	}

	@Transactional(readOnly = true)
	public StoreReviewsRes findByStoreOwnerProviderId(Long providerId) {
		Store store = getStoreByProviderId(providerId);

		if (!store.isSameOwnerAndTheRequester(providerId)) {
			log.warn("GET:READ:NOT A STORE OWNER : STORE OWNER {}, REQUESTER {}",
				store.getMember().getId(), providerId);
			throw new BusinessException(UNAUTHORIZED_REQUEST);
		}

		List<ReviewStatistics> reviews = reviewRepository.selectStatisticsByStoreId(providerId);

		return StoreReviewsRes.of(store.getId(), reviews);
	}

	@Transactional(readOnly = true)
	public ReviewRes findByOrderId(Long id, Long providerId) {
		Order order = getOrder(id);
		checkMemberAuthority(providerId, order);

		List<Review> reviews = reviewRepository.findAllByOrderId(id);

		return ReviewRes.from(reviews);
	}

	private Order getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ORDER_BY_ID : {}", orderId);
				return new EntityNotFoundException(NOT_FOUND_ORDER);
			});
	}

	private Store getStoreByProviderId(Long providerId) {
		return storeRepository.findByMemberProviderId(providerId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_STORE_BY_OWNER_PROVIDER_ID : {}", providerId);
				return new EntityNotFoundException(NOT_FOUND_STORE);
			});
	}

	private void checkMemberAuthority(Long memberProviderId, Order order) {
		if (!order.isMember(memberProviderId)) {
			log.warn("REVIEW:UNAUTHORIZED:ORDERED MEMBER {}, REQUESTER {}",
				order.getMember().getId(), memberProviderId);
			throw new BusinessException(UNAUTHORIZED_REQUEST);
		}
	}

}
