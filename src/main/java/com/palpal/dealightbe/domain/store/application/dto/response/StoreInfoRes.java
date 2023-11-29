package com.palpal.dealightbe.domain.store.application.dto.response;

import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;

public record StoreInfoRes(
	String storeNumber,
	String name,
	String telephone,
	String addressName,
	double xCoordinate,
	double yCoordinate,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
	LocalTime openTime,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
	LocalTime closeTime,

	Set<String> dayOff,
	StoreStatus storeStatus,
	String image
) {

	public static StoreInfoRes from(Store store) {
		Set<String> dayOffNames = store.getDayOffs().stream()
			.map(DayOff::getName)
			.collect(Collectors.toSet());

		return new StoreInfoRes(
			store.getStoreNumber(), store.getName(), store.getTelephone(),
			store.getAddress().getName(), store.getAddress().getXCoordinate(), store.getAddress().getYCoordinate(),
			store.getOpenTime(), store.getCloseTime(), dayOffNames, store.getStoreStatus(), store.getImage());
	}
}
