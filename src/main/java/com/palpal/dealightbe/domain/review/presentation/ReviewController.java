package com.palpal.dealightbe.domain.review.presentation;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.palpal.dealightbe.domain.review.application.ReviewService;
import com.palpal.dealightbe.domain.review.application.dto.request.ReviewCreateReq;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewContentRes;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewCreateRes;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewRes;
import com.palpal.dealightbe.domain.review.application.dto.response.StoreReviewsRes;
import com.palpal.dealightbe.domain.review.domain.ReviewContent;
import com.palpal.dealightbe.global.aop.ProviderId;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping("/orders")
	@ProviderId
	public ResponseEntity<ReviewCreateRes> create(
		Long providerId,
		@Validated @RequestBody ReviewCreateReq request,
		@RequestParam Long id
	) {

		ReviewCreateRes reviewCreateRes = reviewService.create(id, request, providerId);

		return ResponseEntity.ok(reviewCreateRes);
	}

	@GetMapping("/stores/{id}")
	public ResponseEntity<StoreReviewsRes> findByStoreId(
		@PathVariable Long id
	) {

		StoreReviewsRes storeReviewsRes = reviewService.findByStoreId(id);

		return ResponseEntity.ok(storeReviewsRes);
	}

	@GetMapping("/stores")
	@ProviderId
	public ResponseEntity<StoreReviewsRes> findByStoreOwnerProviderId(
		Long providerId
	) {

		StoreReviewsRes storeReviewsRes = reviewService.findByStoreOwnerProviderId(providerId);

		return ResponseEntity.ok(storeReviewsRes);
	}

	@GetMapping("/orders")
	@ProviderId
	public ResponseEntity<ReviewRes> findByOrderId(
		Long providerId,
		@RequestParam Long id
	) {

		ReviewRes reviewRes = reviewService.findByOrderId(id, providerId);

		return ResponseEntity.ok(reviewRes);
	}

	@GetMapping("/contents")
	@ProviderId
	public ResponseEntity<ReviewContentRes> getContents() {
		List<String> messages = Arrays.stream(ReviewContent.values())
			.map(ReviewContent::getMessage)
			.toList();

		ReviewContentRes reviewContentRes = new ReviewContentRes(messages);

		return ResponseEntity.ok(reviewContentRes);
	}
}
