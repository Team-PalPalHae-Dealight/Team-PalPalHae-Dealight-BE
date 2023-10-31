package com.palpal.dealightbe.domain.item.domain;

import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_ITEM_QUANTITY;

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

@Getter
@Entity
@Table(name = "items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private int stock;

	private int discountPrice;

	private int originalPrice;

	private String description;

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
		this.discountPrice = discountPrice;
		this.originalPrice = originalPrice;
		this.description = description;
		this.information = information;
		this.image = image;
		this.store = store;
	}

	public void deductStock(int quantity) {
		if (this.stock < quantity) {
			throw new BusinessException(INVALID_ITEM_QUANTITY);
		}

		this.stock -= quantity;
	}
}
