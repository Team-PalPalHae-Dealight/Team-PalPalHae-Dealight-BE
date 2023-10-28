package com.palpal.dealightbe.domain.store.application.dto.request;

import java.time.LocalDateTime;

import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.store.domain.Store;

public record StoreCreateReq(
	String storeNumber,
	String name,
	String telephone,
	String addressName,
	double xCoordinate,
	double yCoordinate,
	LocalDateTime openTime,
	LocalDateTime closeTime,
	String dayOff
) {

	public static Store toStore(StoreCreateReq request) {
		return Store.builder()
			.storeNumber(request.storeNumber)
			.name(request.name)
			.telephone(request.telephone)
			.address(AddressRes.toAddress(request.addressName, request.xCoordinate, request.yCoordinate))
			.openTime(request.openTime)
			.closeTime(request.closeTime)
			.dayOff(request.dayOff)
			.build();
	}
}
