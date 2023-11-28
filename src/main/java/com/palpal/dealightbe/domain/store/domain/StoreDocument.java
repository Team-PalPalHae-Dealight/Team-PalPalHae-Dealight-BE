package com.palpal.dealightbe.domain.store.domain;

import java.util.List;

import javax.persistence.Id;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import com.palpal.dealightbe.domain.item.domain.ItemDocument;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "store")
@Mapping(mappingPath = "elastic/store-mapping.json")
@Setting(settingPath = "elastic/store-setting.json")
public class StoreDocument {

	@Id
	private String id;

	private GeoPoint location;

	private String name;

	private StoreStatus storeStatus = StoreStatus.CLOSED;

	private String openTime;

	private String closeTime;

	private String image;

	private List<ItemDocument> items;

	public static StoreDocument from(UpdatedStore updatedStore, Store store) {
		return StoreDocument.builder()
			.id(String.valueOf(updatedStore.getId()))
			.location(new GeoPoint(updatedStore.getYCoordinate(), updatedStore.getXCoordinate()))
			.name(updatedStore.getName())
			.storeStatus(StoreStatus.OPENED)
			.openTime(updatedStore.getOpenTime().toString())
			.closeTime(updatedStore.getCloseTime().toString())
			.image(updatedStore.getImage())
			.items(ItemDocument.convertToItemDocuments(store.getItems()))
			.build();
	}
}
