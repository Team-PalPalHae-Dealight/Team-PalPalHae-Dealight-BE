package com.palpal.dealightbe.domain.review.application.dto.response;

import java.util.List;

public record StoreReviewsRes(
	Long storeId,
	List<StoreReviewRes> reviews
) {
	public static StoreReviewsRes of(long id, List<ReviewStatistics> reviews) {
		return new StoreReviewsRes(id,
			reviews.stream()
				.map(r -> new StoreReviewRes(r.getContent(), r.getCount()))
				.toList());
	}
}
