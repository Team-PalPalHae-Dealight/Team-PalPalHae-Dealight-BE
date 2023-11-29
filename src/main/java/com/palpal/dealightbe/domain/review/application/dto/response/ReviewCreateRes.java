package com.palpal.dealightbe.domain.review.application.dto.response;

import java.util.List;

import com.palpal.dealightbe.domain.review.domain.Review;

public record ReviewCreateRes(
	List<Long> ids
) {

	public static ReviewCreateRes from(List<Review> reviews) {
		return new ReviewCreateRes(
			reviews.stream()
				.map(Review::getId)
				.toList()
		);
	}
}
