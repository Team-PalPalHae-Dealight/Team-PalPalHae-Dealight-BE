package com.palpal.dealightbe.domain.review.application.dto.response;

import java.util.List;

import com.palpal.dealightbe.domain.review.domain.ReviewContent;

public record StoreReviewsRes(
	Long storeId,
	List<StoreReviewRes> reviews
) {
	public static StoreReviewsRes of(long storeId, List<ReviewStatistics> reviews) {
		return new StoreReviewsRes(storeId,
			reviews.stream()
				.map(r -> new StoreReviewRes(
					ReviewContent.valueOf(r.getContent()).getMessage(),
					r.getCount())
				).toList());
	}
}
