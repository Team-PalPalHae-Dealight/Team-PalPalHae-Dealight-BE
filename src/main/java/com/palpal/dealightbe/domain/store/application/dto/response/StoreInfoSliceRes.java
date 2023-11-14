package com.palpal.dealightbe.domain.store.application.dto.response;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.palpal.dealightbe.domain.store.domain.Store;

public record StoreInfoSliceRes(
	Long storeId,
	String name,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
	LocalTime closeTime,
	String image
) {

	public static StoreInfoSliceRes from(Store store) {
		return new StoreInfoSliceRes(store.getId(), store.getName(), store.getCloseTime(), store.getImage());
	}
}
