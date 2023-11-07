package com.palpal.dealightbe.domain.item.presentation;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.palpal.dealightbe.domain.image.application.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.item.application.ItemService;
import com.palpal.dealightbe.domain.item.application.dto.request.ItemReq;
import com.palpal.dealightbe.domain.item.application.dto.response.ItemRes;
import com.palpal.dealightbe.domain.item.application.dto.response.ItemsRes;

@RequiredArgsConstructor
@RequestMapping("/api/items")
@RestController
public class ItemController {

	private final ItemService itemService;

	@PostMapping
	public ResponseEntity<ItemRes> create(@Validated @RequestPart ItemReq itemReq, @RequestParam Long memberId, @RequestPart(required = false) MultipartFile image) {
		ImageUploadReq imageUploadReq = new ImageUploadReq(image);

		ItemRes itemRes = itemService.create(itemReq, memberId, imageUploadReq);

		return ResponseEntity.ok(itemRes);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ItemRes> findById(@PathVariable("id") Long itemId) {
		ItemRes itemRes = itemService.findById(itemId);

		return ResponseEntity.ok(itemRes);
	}

	@GetMapping("/stores")
	public ResponseEntity<ItemsRes> findAllForStore(@RequestParam Long memberId, @RequestParam(required = false, defaultValue = "0") int page, @RequestParam(required = false, defaultValue = "5") int size) {
		PageRequest pageable = PageRequest.of(page, size);

		ItemsRes itemsRes = itemService.findAllForStore(memberId, pageable);

		return ResponseEntity.ok(itemsRes);
	}

	@GetMapping("/members")
	public ResponseEntity<ItemsRes> findAllForMember(@RequestParam("x-coordinate") double xCoordinate, @RequestParam("y-coordinate") double yCoordinate, @RequestParam("sort-by") String sortBy, @RequestParam(required = false, defaultValue = "0") int page, @RequestParam(required = false, defaultValue = "5") int size) {
		PageRequest pageable = PageRequest.of(page, size);

		ItemsRes itemsRes = itemService.findAllForMember(xCoordinate, yCoordinate, sortBy, pageable);

		return ResponseEntity.ok(itemsRes);
	}

//	@PatchMapping("/{id}")
//	public ResponseEntity<ItemRes> update(@PathVariable("id") Long itemId, @Validated @RequestBody ItemReq itemReq, @RequestParam Long memberId) {
//		ItemRes itemRes = itemService.update(itemId, itemReq, memberId);
//
//		return ResponseEntity.ok(itemRes);
//	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Long itemId, @RequestParam Long memberId) {
		itemService.delete(itemId, memberId);

		return ResponseEntity.noContent()
			.build();
	}
}
