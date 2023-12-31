package com.palpal.dealightbe.domain.store.application.dto.response;

import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;

public record StoreCreateRes(
	Long id,
	String storeNumber,
	String name,
	String telephone,
	AddressRes addressRes,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
	LocalTime openTime,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
	LocalTime closeTime,

	Set<String> dayOff,
	String imageUrl
) {

	public static StoreCreateRes from(Store store) {
		Set<String> dayOffNames = store.getDayOffs().stream()
			.map(DayOff::getName)
			.collect(Collectors.toSet());

		return new StoreCreateRes(
			store.getId(), store.getStoreNumber(), store.getName(), store.getTelephone(),
			AddressRes.from(store.getAddress()), store.getOpenTime(), store.getCloseTime(), dayOffNames, store.getImage());
	}
}
