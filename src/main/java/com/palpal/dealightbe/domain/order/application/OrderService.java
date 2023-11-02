package com.palpal.dealightbe.domain.order.application;

import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ITEM;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_MEMBER;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ORDER;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_STORE;
import static com.palpal.dealightbe.global.error.ErrorCode.UNAUTHORIZED_REQUEST;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.item.domain.ItemRepository;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderCreateReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderProductReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderStatusUpdateReq;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderRes;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderStatusUpdateRes;
import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderItem;
import com.palpal.dealightbe.domain.order.domain.OrderRepository;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final MemberRepository memberRepository;
	private final StoreRepository storeRepository;
	private final ItemRepository itemRepository;

	public OrderRes create(OrderCreateReq orderCreateReq, Long memberProviderId) {
		Member member = memberRepository.findMemberByProviderId(memberProviderId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_ID : {}", memberProviderId);
				return new EntityNotFoundException(NOT_FOUND_MEMBER);
			});

		Store store = storeRepository.findById(orderCreateReq.storeId())
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_STORE_BY_ID : {}", orderCreateReq.storeId());
				return new EntityNotFoundException(NOT_FOUND_STORE);
			});

		Order order = OrderCreateReq.toOrder(orderCreateReq, member, store);
		orderRepository.save(order);

		List<OrderItem> orderItems = createOrderItems(order, orderCreateReq.orderProductsReq().orderProducts());
		order.addOrderItems(orderItems);

		return OrderRes.from(order);
	}

	public OrderStatusUpdateRes updateStatus(Long orderId, OrderStatusUpdateReq request, Long memberProviderId) {
		Member member = memberRepository.findMemberByProviderId(memberProviderId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_PROVIDER_ID : {}", memberProviderId);

				return new EntityNotFoundException(NOT_FOUND_MEMBER);
			});

		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ORDER_BY_ID : {}", orderId);

				return new EntityNotFoundException(NOT_FOUND_ORDER);
			});

		String originalStatus = order.getOrderStatus().name();
		String changedStatus = request.status();

		order.changeStatus(member, originalStatus, changedStatus);

		return OrderStatusUpdateRes.from(order);
	}

	@Transactional(readOnly = true)
	public OrderRes findById(Long orderId, Long memberProviderId) {
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ORDER_BY_ID : {}", orderId);

				return new EntityNotFoundException(NOT_FOUND_ORDER);
			});

		Member member = memberRepository.findMemberByProviderId(memberProviderId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_PROVIDER_ID : {}", memberProviderId);

				return new EntityNotFoundException(NOT_FOUND_MEMBER);
			});

		if (!(order.isMember(member) || order.isStoreOwner(member))) {
			log.warn("GET:READ:UNAUTHORIZED: ORDERED_MEMBER {}, STORE_OWNER {}, REQUESTER {}",
				member, order.getStore().getMember().getProviderId(), memberProviderId);

			throw new BusinessException(UNAUTHORIZED_REQUEST);
		}

		return OrderRes.from(order);
	}

	private List<OrderItem> createOrderItems(Order order, List<OrderProductReq> orderProductsReq) {
		return orderProductsReq.stream()
			.map(productReq -> createOrderItem(order, productReq))
			.toList();
	}

	private OrderItem createOrderItem(Order order, OrderProductReq request) {
		Item item = itemRepository.findById(request.itemId())
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ITEM_BY_ID : {}", request.itemId());

				return new EntityNotFoundException(NOT_FOUND_ITEM);
			});

		int quantity = request.quantity();

		item.deductStock(quantity);

		return OrderItem.builder()
			.item(item)
			.order(order)
			.quantity(quantity)
			.build();
	}
}
