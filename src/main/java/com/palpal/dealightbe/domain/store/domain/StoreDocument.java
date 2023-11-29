package com.palpal.dealightbe.domain.store.domain;

import javax.persistence.Id;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.palpal.dealightbe.domain.address.domain.Address;

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
	private Long id;

	private Address address;

	private String name;

	private StoreStatus storeStatus = StoreStatus.CLOSED;

	private String openTime;

	private String closeTime;

	private String image;

	public static StoreDocument from(Store store) {
		return StoreDocument.builder()
			.id(store.getId())
			.address(store.getAddress())
			.name(store.getName())
			.openTime(store.getOpenTime().toString())
			.closeTime(store.getCloseTime().toString())
			.image(store.getImage())
			.build();
	}
}
