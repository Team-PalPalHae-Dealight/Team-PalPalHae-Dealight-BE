package com.palpal.dealightbe.domain.cart.application;

import static com.palpal.dealightbe.global.error.ErrorCode.ANOTHER_STORE_ITEM_ALREADY_EXISTS_IN_THE_CART;
import static com.palpal.dealightbe.global.error.ErrorCode.EXCEEDED_CART_ITEM_SIZE;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_CART_ITEM;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ITEM;
import static com.palpal.dealightbe.global.error.ErrorCode.ITEM_REMOVED_NO_LONGER_EXISTS_ITEM;
import static com.palpal.dealightbe.global.error.ErrorCode.ITEM_REMOVED_NO_LONGER_EXISTS_STORE;
import static com.palpal.dealightbe.global.error.ErrorCode.TOO_MANY_CART_REQUESTS;
import static com.palpal.dealightbe.global.error.ErrorCode.UNABLE_TO_ADD_TO_CART_ITEM_STOCK_ZERO;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;
import com.palpal.dealightbe.global.error.exception.ExcessiveRequestException;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

	private static final int MAXIMUM_CART_SIZE = 5;
	private static final int CONSUME_BUCKET_COUNT = 1;

	private final CartRepository cartRepository;
	private final ItemRepository itemRepository;
	private final StoreRepository storeRepository;
	private final Bucket bucket;

	public CartRes addItem(Long providerId, Long itemId, CartAdditionType cartAdditionType) {
		checkBucketCount(bucket);

		Item item = getItem(itemId);
		validateItemStock(item);
		validateOwnStoreItem(providerId, item);

		List<Cart> carts = cartRepository.findAllByMemberProviderIdOrderByItemIdAsc(providerId);
		List<Cart> upToDateCarts = upToDateCarts(carts);

		validateAnotherStoreItemExistence(upToDateCarts, item.getStore().getId(), cartAdditionType);

		return addItem(providerId, item, upToDateCarts, cartAdditionType);
	}

	public CartsRes findAllByProviderId(Long providerId) {
		List<Cart> carts = cartRepository.findAllByMemberProviderIdOrderByItemIdAsc(providerId);

		List<Cart> updatedCarts = upToDateCarts(carts);

		return CartsRes.from(updatedCarts);
	}

	public CartsRes update(Long providerId, CartsReq cartsReq) {
		checkBucketCount(bucket);

		List<Cart> carts = getCarts(cartsReq, providerId);

		List<Cart> renewedCarts = upToDateCarts(carts);
		List<Cart> updatedCarts = updateCartsQuantity(renewedCarts, cartsReq);

		return CartsRes.from(updatedCarts);
	}

	public void deleteOne(Long providerId, Long itemId) {
		Cart cart = getCart(itemId, providerId);

		cartRepository.delete(cart);
	}

	public void deleteAll(Long providerId) {
		List<Cart> carts = cartRepository.findAllByMemberProviderIdOrderByItemIdAsc(providerId);

		cartRepository.deleteAll(carts);
	}

	private List<Cart> upToDateCarts(List<Cart> carts) {
		List<Cart> unexpiredCarts = getUnexpiredCarts(carts);

		return updateCarts(unexpiredCarts);
	}

	private List<Cart> updateCartsQuantity(List<Cart> carts, CartsReq cartsReq) {

		return IntStream.range(0, carts.size())
			.mapToObj(index -> updateCartQuantity(cartsReq, carts, index))
			.toList();
	}

	private Cart updateCartQuantity(CartsReq cartsReq, List<Cart> carts, int index) {
		int quantity = cartsReq.carts().get(index).quantity();
		Cart cart = carts.get(index);

		cart.updateQuantity(quantity);
		return cartRepository.save(cart);
	}

	private List<Cart> updateCarts(List<Cart> carts) {
		List<Cart> updatedCarts = carts.stream()
			.map(this::renewCart)
			.filter(Objects::nonNull)
			.toList();

		compareCartsSize(carts, updatedCarts);

		return updatedCarts;
	}

	private void compareCartsSize(List<Cart> carts, List<Cart> updatedCarts) {
		if (carts.size() != updatedCarts.size()) {
			log.warn("GET:READ:NOT_FOUND_ITEM_BY_ID_AND_ITEM_REMOVED_NO_LONGER_EXISTS_ITEM : carts.size() = {}, updatedCarts.size() = {}", carts.size(), updatedCarts.size());
			throw new EntityNotFoundException(ITEM_REMOVED_NO_LONGER_EXISTS_ITEM);
		}
	}

	private Cart renewCart(Cart cart) {
		Item item = getExistingItem(cart);
		if (item == null) {
			return null;
		}

		cart.update(item.getName(), item.getStock(), item.getDiscountPrice(), item.getImage(), item.getStore().getCloseTime());

		if (!cart.getExpirationDateTime().toLocalTime().equals(item.getStore().getCloseTime())) {
			cart.updateExpirationDateTime();
		}

		return cartRepository.save(cart);
	}

	private boolean isUnexpiredCart(Cart cart) {
		if (cart.isExpired()) {
			deleteStoreClosedCart(cart);

			return false;
		}

		return true;
	}

	private List<Cart> getUnexpiredCarts(List<Cart> carts) {

		return carts.stream()
			.filter(this::isUnexpiredCart)
			.toList();
	}

	private void deleteStoreClosedCart(Cart cart) {
		Store store = getStore(cart);

		if (isStoreClosed(store) || cart.isExpired()) {
			cartRepository.delete(cart);
		}
	}

	private boolean isStoreClosed(Store store) {

		return store.getStoreStatus().equals(StoreStatus.CLOSED);
	}

	private void validateOwnStoreItem(Long providerId, Item item) {
		Store store = item.getStore();
		Member member = store.getMember();

		if (Objects.equals(member.getProviderId(), providerId)) {
			log.warn("POST:CREATE:INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART : providerId = {}, itemId = {}", providerId, item.getId());
			throw new BusinessException(INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART);
		}
	}

	private CartRes addItem(Long providerId, Item item, List<Cart> carts, CartAdditionType cartAdditionType) {
		Cart cart = getCartToAddItem(item, providerId, carts, cartAdditionType);

		Cart savedCart = cartRepository.save(cart);

		return CartRes.from(savedCart);
	}

	private Cart getCartToAddItem(Item item, Long providerId, List<Cart> carts, CartAdditionType cartAdditionType) {

		return cartRepository.findByItemIdAndMemberProviderId(item.getId(), providerId)
			.map(cart -> {
				cart.updateQuantity(cart.getQuantity() + 1);
				return cart;
			})
			.orElseGet(() -> {
				validateExceedCartItemSize(carts, cartAdditionType);

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

	private Item getExistingItem(Cart cart) {

		return itemRepository.findById(cart.getItemId())
			.orElseGet(() -> {
				cartRepository.delete(cart);
				return null;
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

	private Store getStore(Cart cart) {
		return storeRepository.findById(cart.getStoreId())
				.orElseThrow(() -> {
					log.warn("GET:READ:NOT_FOUND_STORE_BY_ID_AND_ITEM_REMOVED_NO_LONGER_EXISTS_STORE : {}", cart.getStoreId());
					deleteAll(cart.getMemberProviderId());

					return new EntityNotFoundException(ITEM_REMOVED_NO_LONGER_EXISTS_STORE);
				});
		}

		private void validateItemStock(Item item) {
			if (item.getStock() == 0) {
				log.warn("GET:READ:UNABLE_TO_ADD_TO_CART_ITEM_STOCK_ZERO : item id = {}", item.getId());
				throw new BusinessException(UNABLE_TO_ADD_TO_CART_ITEM_STOCK_ZERO);
		}
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

	private void checkBucketCount(Bucket bucket) {
		if (bucket.tryConsume(CONSUME_BUCKET_COUNT)) {
			return;
		}

		log.warn("POST/PATCH:CREATE/UPDATE:TOO_MANY_CART_REQUESTS");
		throw new ExcessiveRequestException(TOO_MANY_CART_REQUESTS);
	}
}
