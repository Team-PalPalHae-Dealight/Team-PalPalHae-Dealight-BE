package com.palpal.dealightbe.domain.cart.application;

import static com.palpal.dealightbe.global.error.ErrorCode.ANOTHER_STORE_ITEM_ALREADY_EXISTS_IN_THE_CART;
import static com.palpal.dealightbe.global.error.ErrorCode.EXCEEDED_CART_ITEM_SIZE;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ITEM;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.cart.application.dto.response.CartRes;
import com.palpal.dealightbe.domain.cart.application.dto.response.CartsRes;
import com.palpal.dealightbe.domain.cart.domain.Cart;
import com.palpal.dealightbe.domain.cart.domain.CartAdditionType;
import com.palpal.dealightbe.domain.cart.domain.CartRepository;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.item.domain.ItemRepository;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

	private static final int MAXIMUM_CART_SIZE = 5;

	private final CartRepository cartRepository;
	private final ItemRepository itemRepository;

	public CartRes addItem(Long providerId, Long itemId, CartAdditionType cartAdditionType) {
		Item item = getItem(itemId);

		validateOwnStoreItem(providerId, item);

		List<Cart> carts = cartRepository.findAllByMemberProviderId(providerId);

		validateAnotherStoreItemExistence(carts, item.getStore().getId(), cartAdditionType);

		CartRes cartRes = addItem(providerId, itemId, carts, cartAdditionType);
		return cartRes;
	}

	public CartsRes findAllByProviderId(Long providerId) {
		List<Cart> carts = cartRepository.findAllByMemberProviderId(providerId);

		List<Cart> updatedCarts = carts.stream()
			.map(this::updateStock)
			.sorted(Comparator.comparing(Cart::getItemId))
			.toList();

		return CartsRes.from(updatedCarts);
	}

	private Cart updateStock(Cart cart) {
		Long itemId = cart.getItemId();
		Item item = getItem(itemId);

		cart.updateStock(item.getStock());

		return cartRepository.save(cart);
	}

	private void validateOwnStoreItem(Long providerId, Item item) {
		Store store = item.getStore();
		Member member = store.getMember();

		if (Objects.equals(member.getProviderId(), providerId)) {
			log.warn("POST:CREATE:INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART : providerId = {}, itemId = {}", providerId, item.getId());
			throw new BusinessException(INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART);
		}
	}

	private CartRes addItem(Long providerId, Long itemId, List<Cart> carts, CartAdditionType cartAdditionType) {
		Cart cart = getCartToAddItem(itemId, providerId, carts, cartAdditionType);

		cart.updateExpiration();

		Cart savedCart = cartRepository.save(cart);

		return CartRes.from(savedCart);
	}

	private Cart getCartToAddItem(Long itemId, Long providerId, List<Cart> carts, CartAdditionType cartAdditionType) {

		return cartRepository.findByItemIdAndMemberProviderId(itemId, providerId)
			.map(cart -> {
				cart.updateQuantity(cart.getQuantity() + 1);
				return cart;
			})
			.orElseGet(() -> {
				validateExceedCartItemSize(carts, cartAdditionType);

				Item item = getItem(itemId);
				return toCart(providerId, item);
			});
	}

	private void validateAnotherStoreItemExistence(List<Cart> carts, Long attemptedStoreId, CartAdditionType cartAdditionType) {
		boolean existsAnotherStoreItem = existsAnotherStoreItem(carts, attemptedStoreId);

		if (existsAnotherStoreItem) {
			if (Objects.equals(cartAdditionType, CartAdditionType.BY_CHECK)) {
				log.warn("POST:CREATE:ANOTHER_STORE_ITEM_ALREADY_EXISTS_IN_THE_CART : providerId = {}, existing storeId = {}, attempted storeId = {}", carts.get(0).getMemberProviderId(), carts.get(0).getStoreId(), attemptedStoreId);
				throw new BusinessException(ANOTHER_STORE_ITEM_ALREADY_EXISTS_IN_THE_CART);
			}

			cartRepository.deleteAll(carts);
		}
	}

	private boolean existsAnotherStoreItem(List<Cart> carts, Long attemptedStoreId) {

		return carts.stream()
			.anyMatch(cart -> !Objects.equals(cart.getStoreId(), attemptedStoreId));
	}

	private void validateExceedCartItemSize(List<Cart> carts, CartAdditionType cartAdditionType) {
		if (carts.size() >= MAXIMUM_CART_SIZE) {
			if (Objects.equals(cartAdditionType, CartAdditionType.BY_CHECK)) {
				log.warn("POST:CREATE:EXCEED_CART_ITEM_SIZE : cart size = {}", carts.size());
				throw new BusinessException(EXCEEDED_CART_ITEM_SIZE);
			}

			cartRepository.deleteAll(carts);
		}
	}

	private Item getItem(Long itemId) {

		return itemRepository.findById(itemId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ITEM_BY_ID : {}", itemId);
				return new EntityNotFoundException(NOT_FOUND_ITEM);
			});
	}

	private Cart toCart(Long memberProviderId, Item item) {

		return Cart.builder()
			.itemId(item.getId())
			.storeId(item.getStore().getId())
			.memberProviderId(memberProviderId)
			.itemName(item.getName())
			.stock(item.getStock())
			.discountPrice(item.getDiscountPrice())
			.itemImage(item.getImage())
			.storeName(item.getStore().getName())
			.storeCloseTime(item.getStore().getCloseTime())
			.build();
	}
}
