package com.palpal.dealightbe.domain.item.domain;

import java.util.List;

import javax.persistence.Id;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "item")
@Mapping(mappingPath = "elastic/item-mapping.json")
@Setting(settingPath = "elastic/store-setting.json")
public class ItemDocument {

	@Id
	private String id;

	private String name;

	private String storeId;

	public static ItemDocument from(UpdatedItem item) {
		return ItemDocument.builder()
			.id(String.valueOf(item.getId()))
			.name(item.getName())
			.storeId(String.valueOf(item.getStore().getId()))
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
}
