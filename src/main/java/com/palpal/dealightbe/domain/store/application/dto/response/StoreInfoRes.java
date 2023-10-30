package com.palpal.dealightbe.domain.store.application.dto.response;

import java.time.LocalTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;

public record StoreInfoRes(
	String storeNumber,
	String name,
	String telephone,
	String addressName,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
	LocalTime openTime,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
	LocalTime closeTime,

	Set<DayOff> dayOff,
	StoreStatus storeStatus,
	String image
) {

	public static StoreInfoRes from(Store store) {
		return new StoreInfoRes(
			store.getStoreNumber(), store.getName(), store.getTelephone(), store.getAddress().getName(),
			store.getOpenTime(), store.getCloseTime(), store.getDayOffs(), store.getStoreStatus(), store.getImage());
	}
}
