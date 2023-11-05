package com.palpal.dealightbe.domain.review.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.palpal.dealightbe.domain.review.application.ReviewService;
import com.palpal.dealightbe.domain.review.application.dto.request.ReviewCreateReq;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewCreateRes;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {
	private final ReviewService reviewService;

	@PostMapping("/{memberProviderId}/orders")
	public ResponseEntity<ReviewCreateRes> create(
		@Validated @RequestBody ReviewCreateReq request,
		@PathVariable Long memberProviderId,
		@RequestParam Long id
	) {

		ReviewCreateRes reviewCreateRes = reviewService.create(id, request, memberProviderId);

		return ResponseEntity.ok(reviewCreateRes);
	}

}
