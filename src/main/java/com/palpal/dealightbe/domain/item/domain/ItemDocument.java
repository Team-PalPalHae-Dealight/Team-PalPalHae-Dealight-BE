package com.palpal.dealightbe.domain.item.domain;

import java.util.List;

import javax.persistence.Id;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "item")
@Mapping(mappingPath = "elastic/item-mapping.json")
@Setting(settingPath = "elastic/store-setting.json")
public class ItemDocument {

	@Id
	private String id;

	private String name;

	private String storeId;

	private int discountPrice;

	private int originalPrice;

	private double discountRate;

	@Builder
	public ItemDocument(String id, String name, String storeId, int discountPrice, int originalPrice) {
		this.id = id;
		this.name = name;
		this.storeId = storeId;
		this.discountPrice = discountPrice;
		this.originalPrice = originalPrice;
		this.discountRate = calculateDiscountRate(originalPrice, discountPrice);
	}

	public static ItemDocument from(UpdatedItem item) {
		return ItemDocument.builder()
			.id(String.valueOf(item.getId()))
			.name(item.getName())
			.storeId(String.valueOf(item.getStore().getId()))
			.discountPrice(item.getDiscountPrice())
			.originalPrice(item.getOriginalPrice())
			.build();
	}

	public static List<ItemDocument> convertToItemDocuments(List<UpdatedItem> items) {
		if (items == null) {
			return null;
		}

		return items.stream()
			.map(ItemDocument::from)
			.toList();
	}

	private double calculateDiscountRate(int originalPrice, int discountPrice) {
		if (originalPrice != 0) {
			return discountRate = (originalPrice - discountPrice) / (double) originalPrice * 100.0;
		} else {
			return discountRate = 0.0;
		}
	}
}
