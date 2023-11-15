package com.palpal.dealightbe.domain.cart.domain;

import static com.palpal.dealightbe.global.error.ErrorCode.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import com.palpal.dealightbe.global.error.exception.BusinessException;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("carts")
public class Cart {

	private static final int INITIAL_QUANTITY = 1;

	@Id
	private Long id;

	@Indexed
	private Long itemId;

	private Long storeId;

	@Indexed
	private Long memberProviderId;

	private String itemName;

	private int stock;

	private int discountPrice;

	private String itemImage;

	private int quantity;

	private String storeName;

	private LocalTime storeCloseTime;

	@TimeToLive
	private Long expiration;

	@Builder
	public Cart(Long itemId, Long storeId, Long memberProviderId, String itemName, int stock, int discountPrice, String itemImage, String storeName, LocalTime storeCloseTime) {
		this.itemId = itemId;
		this.storeId = storeId;
		this.memberProviderId = memberProviderId;
		this.itemName = itemName;
		this.stock = stock;
		this.discountPrice = discountPrice;
		this.itemImage = itemImage;
		this.quantity = INITIAL_QUANTITY;
		this.storeName = storeName;
		this.storeCloseTime = storeCloseTime;
		this.expiration = calculateExpiration();
	}

	public void updateExpiration() {
		this.expiration = calculateExpiration();
	}

	public void updateQuantity(int quantity) {
		validateQuantity(quantity, this.stock);

		this.quantity = quantity;
	}

	public void updateStock(int stock) {
		this.stock = stock;
	}

	private void validateQuantity(int quantity, int stock) {
		if (quantity < INITIAL_QUANTITY || quantity > stock) {
			log.warn("INVALID_CART_QUANTITY : quantity = {}, stock = {}", quantity, stock);
			throw new BusinessException(INVALID_CART_QUANTITY);
		}
	}

	private Long calculateExpiration() {
		LocalDateTime currentDateTime = LocalDateTime.now();
		LocalDateTime closeDateTime = getCloseDateTime(currentDateTime);

		return currentDateTime.until(closeDateTime, ChronoUnit.SECONDS);
	}

	private LocalDateTime getCloseDateTime(LocalDateTime currentDateTime) {
		if (this.storeCloseTime.isBefore(currentDateTime.toLocalTime())) {
			return currentDateTime.toLocalDate().atTime(this.storeCloseTime).plusDays(1);
		}

		return currentDateTime.toLocalDate().atTime(this.storeCloseTime);
	}
}
