package com.palpal.dealightbe.domain.cart.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.cart.application.dto.request.CartReq;
import com.palpal.dealightbe.domain.cart.application.dto.response.CartRes;
import com.palpal.dealightbe.domain.cart.domain.Cart;
import com.palpal.dealightbe.domain.cart.domain.CartRepository;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.item.domain.ItemRepository;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import static com.palpal.dealightbe.global.error.ErrorCode.ANOTHER_STORE_ITEM_ALREADY_EXISTS_IN_THE_CART;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ITEM;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;
	private final ItemRepository itemRepository;

	public CartRes checkAndAddItem(Long providerId, Long itemId) {
		Item item = getItem(itemId);

		validateOwnStoreItem(providerId, item);

		List<Cart> carts = cartRepository.findAllByMemberProviderId(providerId);

		validateAnotherStoreItemExistence(carts, item.getStore().getId());

		return addItem(providerId, itemId);
	}

	public CartRes clearAndAddItem(Long providerId, Long itemId) {
		Item item = getItem(itemId);

		validateOwnStoreItem(providerId, item);

		List<Cart> carts = cartRepository.findAllByMemberProviderId(providerId);

		clearAnotherStoreItem(carts, item.getStore().getId());

		return addItem(providerId, itemId);
	}

	private void validateOwnStoreItem(Long providerId, Item item) {
		Store store = item.getStore();
		Member member = store.getMember();

		if (Objects.equals(member.getProviderId(), providerId)) {
			log.warn("POST:CREATE:INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART : providerId = {}, itemId = {}", providerId, item.getId());
			throw new BusinessException(INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART);
		}
	}

	private void clearAnotherStoreItem(List<Cart> carts, Long attemptedStoreId) {
		boolean existsAnotherStoreItem = existsAnotherStoreItem(carts, attemptedStoreId);

		if (existsAnotherStoreItem) {
			cartRepository.deleteAll(carts);
		}
	}

	private CartRes addItem(Long providerId, Long itemId) {
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

	private void validateAnotherStoreItemExistence(List<Cart> carts, Long attemptedStoreId) {
		boolean existsAnotherStoreItem = existsAnotherStoreItem(carts, attemptedStoreId);

		if (existsAnotherStoreItem) {
			log.warn("POST:CREATE:ANOTHER_STORE_ITEM_ALREADY_EXISTS_IN_THE_CART : providerId = {}, existing storeId = {}, attempted storeId = {}", carts.get(0).getMemberProviderId(), carts.get(0).getStoreId(), attemptedStoreId);
			throw new BusinessException(ANOTHER_STORE_ITEM_ALREADY_EXISTS_IN_THE_CART);
		}
	}

	private boolean existsAnotherStoreItem(List<Cart> carts, Long attemptedStoreId) {

		return carts.stream()
			.anyMatch(cart -> !Objects.equals(cart.getStoreId(), attemptedStoreId));
	}

	private Item getItem(Long itemId) {

		return itemRepository.findById(itemId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ITEM_BY_ID : {}", itemId);
				return new EntityNotFoundException(NOT_FOUND_ITEM);
			});
	}
}
