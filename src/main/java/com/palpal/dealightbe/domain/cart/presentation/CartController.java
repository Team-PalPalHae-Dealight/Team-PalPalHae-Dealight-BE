package com.palpal.dealightbe.domain.cart.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.palpal.dealightbe.domain.cart.application.CartService;
import com.palpal.dealightbe.domain.cart.application.dto.response.CartRes;
import com.palpal.dealightbe.global.aop.ProviderId;

@RestController
@RequestMapping("/api/orders/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

	private final CartService cartService;

	@ProviderId
	@PostMapping
	public ResponseEntity<CartRes> checkAndAddItem(Long providerId, @RequestParam("id") Long itemId) {
		CartRes cartRes = cartService.checkAndAddItem(providerId, itemId);

		return ResponseEntity.ok(cartRes);
	}

	@ProviderId
	@PostMapping("/clear")
	public ResponseEntity<CartRes> clearAndAddItem(Long providerId, @RequestParam("id") Long itemId) {
		CartRes cartRes = cartService.clearAndAddItem(providerId, itemId);

		return ResponseEntity.ok(cartRes);
	}
}
