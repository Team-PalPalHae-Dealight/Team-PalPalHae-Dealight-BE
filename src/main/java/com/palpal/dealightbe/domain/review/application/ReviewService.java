package com.palpal.dealightbe.domain.review.application;

import static com.palpal.dealightbe.global.error.ErrorCode.ILLEGAL_REVIEW_REQUEST;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_REVIEW_CREATOR;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ORDER;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderRepository;
import com.palpal.dealightbe.domain.review.application.dto.request.ReviewCreateReq;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewCreateRes;
import com.palpal.dealightbe.domain.review.domain.Review;
import com.palpal.dealightbe.domain.review.domain.ReviewRepository;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {
	private final OrderRepository orderRepository;

	private final ReviewRepository reviewRepository;

	public ReviewCreateRes create(Long orderId, ReviewCreateReq request, Long memberProviderId) {
		Order order = getOrder(orderId);

		if (!order.isMember(memberProviderId)) {
			throw new BusinessException(INVALID_REVIEW_CREATOR);
		}

		if (!order.isCompleted()) {
			throw new BusinessException(ILLEGAL_REVIEW_REQUEST);
		}

		List<Review> reviews = ReviewCreateReq.toReviews(request, order);

		reviewRepository.saveAll(reviews);

		return ReviewCreateRes.from(reviews);
	}

	private Order getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ORDER_BY_ID : {}", orderId);
				return new EntityNotFoundException(NOT_FOUND_ORDER);
			});
	}
}
