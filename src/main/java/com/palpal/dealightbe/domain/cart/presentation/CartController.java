package com.palpal.dealightbe.domain.cart.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.palpal.dealightbe.domain.cart.application.CartService;
import com.palpal.dealightbe.domain.cart.application.dto.request.CartsReq;
import com.palpal.dealightbe.domain.cart.application.dto.response.CartRes;
import com.palpal.dealightbe.domain.cart.application.dto.response.CartsRes;
import com.palpal.dealightbe.domain.cart.domain.CartAdditionType;
import com.palpal.dealightbe.global.aop.ProviderId;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

	private final CartService cartService;

	@ProviderId
	@PostMapping("/items")
	public ResponseEntity<CartRes> addItem(Long providerId, @RequestParam("id") Long itemId, @RequestParam("type") String cartAdditionType) {
		CartAdditionType additionType = CartAdditionType.findCartAdditionType(cartAdditionType);

		CartRes cartRes = cartService.addItem(providerId, itemId, additionType);

		return ResponseEntity.ok(cartRes);
	}

	@ProviderId
	@GetMapping
	public ResponseEntity<CartsRes> findAllByProviderId(Long providerId) {
		CartsRes cartsRes = cartService.findAllByProviderId(providerId);

		return ResponseEntity.ok(cartsRes);
	}

	@ProviderId
	@PatchMapping
	public ResponseEntity<CartsRes> update(Long providerId, @RequestBody CartsReq cartsReq) {
		CartsRes cartsRes = cartService.update(providerId, cartsReq);

		return ResponseEntity.ok(cartsRes);
	}
}
