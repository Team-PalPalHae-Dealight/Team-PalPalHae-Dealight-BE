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

	private StoreStatus storeStatus;

	private String openTime;

	private String closeTime;

	private String image;

	private List<ItemDocument> items;

	public static StoreDocument from(UpdatedStore updatedStore) {
		return StoreDocument.builder()
			.id(String.valueOf(updatedStore.getId()))
			.location(new GeoPoint(updatedStore.getYCoordinate(), updatedStore.getXCoordinate()))
			.name(updatedStore.getName())
			.storeStatus(updatedStore.getStoreStatus())
			.openTime(updatedStore.getOpenTime().toString())
			.closeTime(updatedStore.getCloseTime().toString())
			.image(updatedStore.getImage())
			.items(ItemDocument.convertToItemDocuments(updatedStore.getItems()))
			.build();
	}

	public static List<StoreDocument> convertToStoreDocuments(List<UpdatedStore> stores) {
		if (stores == null) {
			return null;
		}

		return stores.stream()
			.map(StoreDocument::from)
			.toList();
	}
}
