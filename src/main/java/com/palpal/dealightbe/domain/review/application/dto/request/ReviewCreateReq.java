package com.palpal.dealightbe.domain.review.application.dto.request;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.review.domain.Review;
import com.palpal.dealightbe.domain.review.domain.ReviewContent;

public record ReviewCreateReq(
	@NotNull(message = "리뷰를 입력해 주세요")
	@Size(min = 1, message = "리뷰를 한 개 이상 입력해 주세요")
	List<String> messages
) {

	public static List<Review> toReviews(ReviewCreateReq request, Order order) {
		return request.messages.stream()
			.distinct()
			.map(message ->
				Review.builder()
					.order(order)
					.content(ReviewContent.messageOf(message))
					.build()
			).toList();
	}
}
