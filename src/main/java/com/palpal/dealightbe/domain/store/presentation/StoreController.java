package com.palpal.dealightbe.domain.store.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.palpal.dealightbe.domain.store.application.StoreService;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreCreateReq;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreCreateRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreInfoRes;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/stores")
public class StoreController {

	private final StoreService storeService;

	@PostMapping("/{memberId}")
	public ResponseEntity<StoreCreateRes> register(@PathVariable Long memberId, @RequestBody @Validated StoreCreateReq req) {
		StoreCreateRes storeCreateRes = storeService.register(memberId, req);

		return ResponseEntity.ok(storeCreateRes);
	}

	@GetMapping("/profiles/{memberId}/{storeId}")
	public ResponseEntity<StoreInfoRes> getInfo(@PathVariable Long memberId, @PathVariable Long storeId) {
		StoreInfoRes infoRes = storeService.getInfo(memberId, storeId);

		return ResponseEntity.ok(infoRes);
	}
}
