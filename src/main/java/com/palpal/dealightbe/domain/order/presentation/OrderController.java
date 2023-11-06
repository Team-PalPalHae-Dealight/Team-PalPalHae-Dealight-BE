package com.palpal.dealightbe.domain.order.presentation;

import java.net.URI;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.palpal.dealightbe.domain.order.application.OrderService;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderCreateReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderStatusUpdateReq;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderRes;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderStatusUpdateRes;
import com.palpal.dealightbe.domain.order.application.dto.response.OrdersRes;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	private static final String DEFAULT_PAGING_SIZE = "10";
	private static final String DEFAULT_SORTING = "created_at";

	@PostMapping("/{memberProviderId}")
	public ResponseEntity<OrderRes> create(
		@Validated @RequestBody OrderCreateReq request,
		@PathVariable Long memberProviderId
	) {

		OrderRes orderRes = orderService.create(request, memberProviderId);

		URI uri = ServletUriComponentsBuilder
			.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(orderRes.orderId())
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

	@GetMapping("/{orderId}/{memberProviderId}")
	public ResponseEntity<OrderRes> findById(
		@PathVariable Long orderId,
		@PathVariable Long memberProviderId
	) {

		OrderRes orderRes = orderService.findById(orderId, memberProviderId);

		return ResponseEntity.ok(orderRes);
	}

	@GetMapping("/{memberProviderId}/stores")
	public ResponseEntity<OrdersRes> findAllByStoreId(
		@PathVariable Long memberProviderId,
		@RequestParam Long id,
		@RequestParam(required = false) String status,
		@RequestParam(required = false, defaultValue = "0") int page,
		@RequestParam(required = false, defaultValue = DEFAULT_PAGING_SIZE) int size
	) {

		Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORTING).descending());

		OrdersRes ordersRes = orderService.findAllByStoreId(id, memberProviderId, status, pageable);

		return ResponseEntity.ok(ordersRes);
	}

	@GetMapping("/{memberProviderId}")
	public ResponseEntity<OrdersRes> findAllByMemberProviderId(
		@PathVariable Long memberProviderId,
		@RequestParam(required = false) String status,
		@RequestParam(required = false, defaultValue = "0") int page,
		@RequestParam(required = false, defaultValue = DEFAULT_PAGING_SIZE) int size
	) {

		Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORTING).descending());

		OrdersRes ordersRes = orderService.findAllByMemberProviderId(memberProviderId, status, pageable);

		return ResponseEntity.ok(ordersRes);
	}
}
