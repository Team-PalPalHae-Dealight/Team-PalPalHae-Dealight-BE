package com.palpal.dealightbe.domain.cart.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.cart.application.dto.request.CartReq;
import com.palpal.dealightbe.domain.cart.application.dto.response.CartRes;
import com.palpal.dealightbe.domain.cart.domain.Cart;
import com.palpal.dealightbe.domain.cart.domain.CartRepository;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.item.domain.ItemRepository;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ITEM;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;
	private final ItemRepository itemRepository;

	public CartRes addItem(Long providerId, Long itemId) {
		Cart cart = getCartToAddItem(itemId, providerId);

		cart.updateExpiration();

		Cart savedCart = cartRepository.save(cart);

		return CartRes.from(savedCart);
	}

	private Cart getCartToAddItem(Long itemId, Long providerId) {

		return cartRepository.findByItemIdAndMemberProviderId(itemId, providerId)
			.map(cart -> {
				cart.updateQuantity(cart.getQuantity() + 1);
				return cart;
			})
			.orElseGet(() -> {
				Item item = getItem(itemId);
				return CartReq.toCart(providerId, item);
			});
	}

	private Item getItem(Long itemId) {

		return itemRepository.findById(itemId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ITEM_BY_ID : {}", itemId);
				return new EntityNotFoundException(NOT_FOUND_ITEM);
			});
	}
}
