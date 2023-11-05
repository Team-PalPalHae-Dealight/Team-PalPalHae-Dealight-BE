package com.palpal.dealightbe.domain.review.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.palpal.dealightbe.domain.review.application.ReviewService;
import com.palpal.dealightbe.domain.review.application.dto.request.ReviewCreateReq;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewCreateRes;
import com.palpal.dealightbe.global.aop.ProviderId;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {
	private final ReviewService reviewService;

	@PostMapping("orders")
	@ProviderId
	public ResponseEntity<ReviewCreateRes> create(
		Long providerId,
		@Validated @RequestBody ReviewCreateReq request,
		@RequestParam Long id
	) {

		ReviewCreateRes reviewCreateRes = reviewService.create(id, request, providerId);

		return ResponseEntity.ok(reviewCreateRes);
	}

}
