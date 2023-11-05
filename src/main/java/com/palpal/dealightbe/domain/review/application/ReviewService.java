package com.palpal.dealightbe.domain.review.application;

import static com.palpal.dealightbe.global.error.ErrorCode.ILLEGAL_REVIEW_REQUEST;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_REVIEW_CREATOR;
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
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewStatistics;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewsRes;
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

		if (!order.isMember(memberProviderId)) {
			log.warn("POST:WRITER:CANNOT WRITE REVIEW : ORDER {}, REQUESTER {}",
				order.getStore().getMember().getId(), memberProviderId);
			throw new BusinessException(INVALID_REVIEW_CREATOR);
		}

		if (!order.isCompleted()) {
			log.warn("POST:WRITER:CANNOT_WRITER_REVIEW: ORDER_STATUS {}", order.getOrderStatus());
			throw new BusinessException(ILLEGAL_REVIEW_REQUEST);
		}

		List<Review> reviews = ReviewCreateReq.toReviews(request, order);
		reviewRepository.saveAll(reviews);

		return ReviewCreateRes.from(reviews);
	}

	public ReviewsRes findByStoreId(Long id, Long providerId) {
		Store store = getStore(id);

		if (!store.isSameOwnerAndTheRequester(providerId)) {
			log.warn("GET:READ:NOT A STORE OWNER : STORE OWNER {}, REQUESTER {}",
				store.getMember().getId(), providerId);
			throw new BusinessException(UNAUTHORIZED_REQUEST);
		}

		List<ReviewStatistics> reviews = reviewRepository.selectStatisticsByStoreId(id);

		return ReviewsRes.of(id, reviews);
	}

	private Order getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ORDER_BY_ID : {}", orderId);
				return new EntityNotFoundException(NOT_FOUND_ORDER);
			});
	}

	private Store getStore(Long storeId) {
		return storeRepository.findById(storeId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_STORE_BY_ID : {}", storeId);
				return new EntityNotFoundException(NOT_FOUND_STORE);
			});
	}
}
