package com.palpal.dealightbe.domain.store.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.palpal.dealightbe.domain.image.application.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.image.application.dto.response.ImageRes;
import com.palpal.dealightbe.domain.store.application.StoreService;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreCreateReq;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreStatusReq;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreUpdateReq;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreCreateRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreInfoRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreStatusUpdateRes;

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

	@PatchMapping("/profiles/{memberId}/{storeId}")
	public ResponseEntity<StoreInfoRes> updateInfo(@PathVariable Long memberId, @PathVariable Long storeId, @RequestBody @Validated StoreUpdateReq updateReq) {
		StoreInfoRes storeInfoRes = storeService.updateInfo(memberId, storeId, updateReq);

		return ResponseEntity.ok(storeInfoRes);
	}

	@PatchMapping("/status/{memberId}/{storeId}")
	public ResponseEntity<StoreStatusUpdateRes> updateStatus(@PathVariable Long memberId, @PathVariable Long storeId, @RequestBody StoreStatusReq req) {
		StoreStatusUpdateRes storeStatusUpdateRes = storeService.updateStatus(memberId, storeId, req);

		return ResponseEntity.ok(storeStatusUpdateRes);
	}

	@PostMapping("/images/{memberId}/{storeId}")
	public ResponseEntity<ImageRes> uploadImage(@PathVariable Long memberId, @PathVariable Long storeId, MultipartFile file) {
		ImageUploadReq imageUploadReq = new ImageUploadReq(file);
		ImageRes imageRes = storeService.uploadImage(memberId, storeId, imageUploadReq);

		return ResponseEntity.ok(imageRes);
	}
}
