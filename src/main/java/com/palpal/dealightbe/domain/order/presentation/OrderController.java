package com.palpal.dealightbe.domain.order.presentation;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.palpal.dealightbe.domain.order.application.OrderService;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderCreateReq;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderRes;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;

	@PostMapping("/{memberProviderId}")
	public ResponseEntity<OrderRes> create(
		@Validated @RequestBody OrderCreateReq orderCreateReq,
		@PathVariable Long memberProviderId
	) {

		OrderRes orderRes = orderService.create(orderCreateReq, memberProviderId);

		URI uri = ServletUriComponentsBuilder
			.fromCurrentRequest()
			.path("")
			.buildAndExpand()
			.toUri();

		return ResponseEntity.created(uri)
			.body(orderRes);
	}
}
