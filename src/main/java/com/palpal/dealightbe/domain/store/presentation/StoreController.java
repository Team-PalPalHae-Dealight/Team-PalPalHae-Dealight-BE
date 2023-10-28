package com.palpal.dealightbe.domain.store.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.palpal.dealightbe.domain.store.application.StoreService;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreCreateReq;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreRes;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/stores")
public class StoreController {

	private final StoreService storeService;

	@PostMapping("/{memberId}")
	public ResponseEntity<StoreRes> register(@PathVariable Long memberId, @RequestBody @Validated StoreCreateReq req) {
		StoreRes storeRes = storeService.register(memberId, req);

		return ResponseEntity.ok(storeRes);
	}
}
