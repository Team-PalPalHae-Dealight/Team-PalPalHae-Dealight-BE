package com.palpal.dealightbe.domain.item.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.BaseEntity;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.palpal.dealightbe.global.error.ErrorCode.*;

@Getter
@Entity
@Table(name = "items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 50, nullable = false)
	private String name;

	private int stock;

	private int discountPrice;

	private int originalPrice;

	@Column(length = 300)
	private String description;

	@Column(length = 300)
	private String information;

	private String image;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id")
	private Store store;

	@Builder
	public Item(String name, int stock, int discountPrice, int originalPrice, String description, String information,
				String image, Store store) {
		this.name = name;
		this.stock = stock;
		this.discountPrice = validateDiscountPrice(discountPrice, originalPrice);
		this.originalPrice = originalPrice;
		this.description = description;
		this.information = information;
		this.image = image;
		this.store = store;
	}

	private int validateDiscountPrice(int discountPrice, int originalPrice) {
		if (discountPrice > originalPrice) {
			throw new BusinessException(INVALID_ITEM_DISCOUNT_PRICE);
		}

		return discountPrice;
	}
}
