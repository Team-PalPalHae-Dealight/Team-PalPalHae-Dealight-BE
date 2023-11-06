package com.palpal.dealightbe.domain.review.domain;

import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_REVIEW_MESSAGE;

import java.util.Arrays;

import com.palpal.dealightbe.global.error.exception.BusinessException;

public enum ReviewContent {
	Q1("상품 상태가 좋아요"),
	Q2("사장님이 친절해요"),
	Q3("특별한 상품이 있어요"),
	Q4("가격이 저렴해요"),
	Q5("게시된 설명이 자세하고 실제 상품과 동일해요.");

	private final String message;

	public String getMessage() {
		return message;
	}

	ReviewContent(String message) {
		this.message = message;
	}

	public static ReviewContent messageOf(String input) {
		return Arrays.stream(ReviewContent.values())
			.filter(value -> input.equals(value.getMessage()))
			.findFirst()
			.orElseThrow(() -> new BusinessException(INVALID_REVIEW_MESSAGE));
	}
}
