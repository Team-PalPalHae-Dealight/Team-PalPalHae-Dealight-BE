package com.palpal.dealightbe.domain.cart.application;

import static com.palpal.dealightbe.global.error.ErrorCode.ANOTHER_STORE_ITEM_ALREADY_EXISTS_IN_THE_CART;
import static com.palpal.dealightbe.global.error.ErrorCode.EXCEEDED_CART_ITEM_SIZE;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_CART_ITEM;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ITEM;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.cart.application.dto.request.CartsReq;
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

		return addItem(providerId, itemId, carts, cartAdditionType);
	}

	public CartsRes findAllByProviderId(Long providerId) {
		List<Cart> carts = cartRepository.findAllByMemberProviderId(providerId);

		List<Cart> updatedCarts = updateCarts(carts);

		List<Cart> unexpiredCarts = getUnexpiredCarts(updatedCarts);

		return CartsRes.from(unexpiredCarts);
	}

	public CartsRes update(Long providerId, CartsReq cartsReq) {
		List<Cart> carts = getCarts(cartsReq, providerId);

		List<Cart> updatedCarts = IntStream.range(0, carts.size())
			.mapToObj(index -> {
				int quantity = cartsReq.carts().get(index).quantity();
				Cart cart = carts.get(index);

				cart.updateQuantity(quantity);
				return cartRepository.save(cart);
			}).toList();

		return CartsRes.from(updatedCarts);
	}

	private List<Cart> updateCarts(List<Cart> carts) {

		return carts.stream()
			.map(this::renewCart)
			.sorted(Comparator.comparing(Cart::getItemId))
			.toList();
	}

	private Cart renewCart(Cart cart) {
		Long itemId = cart.getItemId();
		Item item = getItem(itemId);

		cart.update(item.getName(), item.getStock(), item.getDiscountPrice(), item.getImage(), item.getStore().getCloseTime());

		if (!cart.getExpirationDateTime().toLocalTime().equals(item.getStore().getCloseTime())) {
			cart.updateExpirationDateTime();
		}

		return cartRepository.save(cart);
	}

	private List<Cart> getUnexpiredCarts(List<Cart> carts) {

		return carts.stream()
			.peek(this::deleteExpiredCart)
			.filter(cart -> !cart.isExpired())
			.toList();
	}

	private void deleteExpiredCart(Cart cart) {
		if (cart.isExpired()) {
			cartRepository.delete(cart);
		}
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

	private List<Cart> getCarts(CartsReq cartsReq, Long providerId) {

		return cartsReq.carts().stream()
			.map(cartReq -> getCart(cartReq.itemId(), providerId))
			.toList();
	}

	private Cart getCart(Long itemId, Long providerId) {

		return cartRepository.findByItemIdAndMemberProviderId(itemId, providerId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_CART_BY_ITEM_ID_AND_PROVIDER_ID : itemId = {}, providerId = {}", itemId, providerId);
				return new EntityNotFoundException(NOT_FOUND_CART_ITEM);
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
