package com.palpal.dealightbe.domain.search.presentation;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.palpal.dealightbe.domain.search.application.SearchService;
import com.palpal.dealightbe.domain.store.application.dto.response.StoresInfoSliceRes;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/search")
public class SearchController {

	private final SearchService searchService;
	private static final String DEFAULT_PAGING_SIZE = "10";

	@PostMapping("/bulk")
	public ResponseEntity<Void> uploadToES() {
		searchService.uploadToES();
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/update")
	public ResponseEntity<Void> updateStatusToES() {
		searchService.updateStatusToES();
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<StoresInfoSliceRes> searchByES(
		@RequestParam("x-coordinate") double xCoordinate, @RequestParam("y-coordinate") double yCoordinate,
		@RequestParam String keyword,
		@RequestParam(required = false, defaultValue = "0") int page,
		@RequestParam(required = false, defaultValue = DEFAULT_PAGING_SIZE) int size) {

		page = Math.max(page - 1, 0);
		PageRequest pageable = PageRequest.of(page, size);

		StoresInfoSliceRes storeResponse = searchService.searchToES(xCoordinate, yCoordinate, keyword, pageable);

		return ResponseEntity.ok(storeResponse);
	}

}
