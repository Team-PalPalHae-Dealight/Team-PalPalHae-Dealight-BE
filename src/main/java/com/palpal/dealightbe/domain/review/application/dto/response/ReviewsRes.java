package com.palpal.dealightbe.domain.review.application.dto.response;

import java.util.List;

public record ReviewsRes(
	Long storeId,
	List<ReviewRes> reviews
) {
	public static ReviewsRes of(long id, List<ReviewStatistics> reviews) {
		return new ReviewsRes(id,
			reviews.stream()
				.map(r -> new ReviewRes(r.getContent(), r.getCount()))
				.toList());
	}
}
