package com.palpal.dealightbe.domain.order.presentation;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.palpal.dealightbe.domain.order.application.OrderService;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderCreateReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderStatusUpdateReq;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderRes;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderStatusUpdateRes;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	@PostMapping("/{memberProviderId}")
	public ResponseEntity<OrderRes> create(
		@Validated @RequestBody OrderCreateReq request,
		@PathVariable Long memberProviderId
	) {

		OrderRes orderRes = orderService.create(request, memberProviderId);

		URI uri = ServletUriComponentsBuilder
			.fromCurrentRequest()
			.path("")
			.buildAndExpand()
			.toUri();

		return ResponseEntity.created(uri)
			.body(orderRes);
	}

	@PatchMapping("/{orderId}/status/{memberProviderId}")
	public ResponseEntity<OrderStatusUpdateRes> updateStatus(
		@Validated @RequestBody OrderStatusUpdateReq request,
		@PathVariable Long orderId,
		@PathVariable Long memberProviderId
	) {

		OrderStatusUpdateRes orderStatusUpdateRes = orderService.updateStatus(orderId, request, memberProviderId);

		return ResponseEntity.ok(orderStatusUpdateRes);
	}
}
