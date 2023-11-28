package com.palpal.dealightbe.domain.store.domain;

import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.palpal.dealightbe.global.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "updated_stores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdatedStore extends BaseEntity {

	@Id
	private Long id;

	private double xCoordinate;
	private double yCoordinate;
	private String name;

	@Enumerated(EnumType.STRING)
	private StoreStatus storeStatus;

	private LocalTime openTime;

	private LocalTime closeTime;

	private String image;

	@Builder
	public UpdatedStore(Long id, double xCoordinate, double yCoordinate, String name, StoreStatus storeStatus,
						LocalTime openTime, LocalTime closeTime, String image) {
		this.id = id;
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
		this.name = name;
		this.storeStatus = storeStatus;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.image = image;
	}

	public static UpdatedStore from(Store store) {
		return UpdatedStore.builder()
			.id(store.getId())
			.xCoordinate(store.getAddress().getXCoordinate())
			.yCoordinate(store.getAddress().getYCoordinate())
			.name(store.getName())
			.storeStatus(store.getStoreStatus())
			.openTime(store.getOpenTime())
			.closeTime(store.getCloseTime())
			.image(store.getImage())
			.build();
	}
}
