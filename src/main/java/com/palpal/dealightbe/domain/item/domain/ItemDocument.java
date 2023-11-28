package com.palpal.dealightbe.domain.item.domain;

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
	private Long id;

	private String name;

	private Long storeId;

	public static ItemDocument from(Item item) {
		return ItemDocument.builder()
			.id(item.getId())
			.name(item.getName())
			.storeId(item.getStore().getId())
			.build();
	}
}
