package com.palpal.dealightbe.domain.review.application.dto.response;

import java.util.List;

import com.palpal.dealightbe.domain.review.domain.Review;

public record ReviewRes(
	List<String> messages
) {
	public static ReviewRes from(List<Review> review) {
		return new ReviewRes(
			review.stream()
				.map(Review::getContent)
				.toList()
		);
	}
}
