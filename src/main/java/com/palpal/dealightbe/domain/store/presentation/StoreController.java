package com.palpal.dealightbe.domain.store.presentation;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.palpal.dealightbe.domain.image.application.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.image.application.dto.response.ImageRes;
import com.palpal.dealightbe.domain.store.application.StoreService;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreCreateReq;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreStatusReq;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreUpdateReq;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreByMemberRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreCreateRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreInfoRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreStatusRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoresInfoSliceRes;
import com.palpal.dealightbe.global.aop.ProviderId;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/stores")
public class StoreController {

	private final StoreService storeService;
	private static final String DEFAULT_PAGING_SIZE = "10";

	@ProviderId
	@PostMapping
	public ResponseEntity<StoreCreateRes> register(Long providerId, @RequestBody @Validated StoreCreateReq req) {
		StoreCreateRes storeCreateRes = storeService.register(providerId, req);

		return ResponseEntity.ok(storeCreateRes);
	}

	@ProviderId
	@GetMapping("/profiles/{storeId}")
	public ResponseEntity<StoreInfoRes> getInfo(Long providerId, @PathVariable Long storeId) {
		StoreInfoRes infoRes = storeService.getInfo(providerId, storeId);

		return ResponseEntity.ok(infoRes);
	}

	@ProviderId
	@PatchMapping("/profiles/{storeId}")
	public ResponseEntity<StoreInfoRes> updateInfo(Long providerId, @PathVariable Long storeId, @RequestBody @Validated StoreUpdateReq updateReq) {
		StoreInfoRes storeInfoRes = storeService.updateInfo(providerId, storeId, updateReq);

		return ResponseEntity.ok(storeInfoRes);
	}

	@ProviderId
	@PatchMapping("/status/{storeId}")
	public ResponseEntity<StoreStatusRes> updateStatus(Long providerId, @PathVariable Long storeId, @RequestBody StoreStatusReq req) {
		StoreStatusRes storeStatusRes = storeService.updateStatus(providerId, storeId, req);

		return ResponseEntity.ok(storeStatusRes);
	}

	@ProviderId
	@GetMapping("/status/{storeId}")
	public ResponseEntity<StoreStatusRes> getStatus(Long providerId, @PathVariable Long storeId) {
		StoreStatusRes status = storeService.getStatus(providerId, storeId);

		return ResponseEntity.ok(status);
	}

	@ProviderId
	@PostMapping("/images/{storeId}")
	public ResponseEntity<ImageRes> uploadImage(Long providerId, @PathVariable Long storeId, MultipartFile file) {
		ImageUploadReq imageUploadReq = new ImageUploadReq(file);
		ImageRes imageRes = storeService.uploadImage(providerId, storeId, imageUploadReq);

		return ResponseEntity.ok(imageRes);
	}

	@ProviderId
	@PatchMapping("/images/{storeId}")
	public ResponseEntity<ImageRes> updateImage(Long providerId, @PathVariable Long storeId, MultipartFile file) {
		ImageUploadReq imageUpdateReq = new ImageUploadReq(file);

		ImageRes imageRes = storeService.updateImage(providerId, storeId, imageUpdateReq);

		return ResponseEntity.ok(imageRes);
	}

	@ProviderId
	@DeleteMapping("/images/{storeId}")
	public ResponseEntity<Void> deleteImage(Long providerId, @PathVariable Long storeId) {
		storeService.deleteImage(providerId, storeId);

		return ResponseEntity.noContent()
			.build();
	}

	@ProviderId
	@GetMapping("/confirm")
	public ResponseEntity<StoreByMemberRes> findByProviderId(Long providerId) {
		StoreByMemberRes storeByMemberRes = storeService.findByProviderId(providerId);

		return ResponseEntity.ok(storeByMemberRes);
	}

	@GetMapping("/search")
	public ResponseEntity<StoresInfoSliceRes> searchByOption(
		@RequestParam("x-coordinate") double xCoordinate, @RequestParam("y-coordinate") double yCoordinate,
		@RequestParam String keyword,
		@RequestParam(required = false, defaultValue = "distance") String sortBy,
		@RequestParam(required = false) Long lastId,
		@RequestParam(required = false, defaultValue = "0") int page,
		@RequestParam(required = false, defaultValue = DEFAULT_PAGING_SIZE) int size) {

		page = Math.max(page - 1, 0);
		PageRequest pageable = PageRequest.of(page, size);

		StoresInfoSliceRes storeResponse = storeService.search(xCoordinate, yCoordinate, keyword, sortBy, lastId, pageable);

		return ResponseEntity.ok(storeResponse);
	}

}
