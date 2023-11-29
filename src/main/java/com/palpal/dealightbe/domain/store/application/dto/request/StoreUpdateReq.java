package com.palpal.dealightbe.domain.store.application.dto.request;

import java.time.LocalTime;
import java.util.Set;

import javax.validation.constraints.Pattern;

import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;

public record StoreUpdateReq(
	@Pattern(regexp = "\\d+")
	String telephone,
	String addressName,
	double xCoordinate,
	double yCoordinate,
	LocalTime openTime,
	LocalTime closeTime,
	Set<DayOff> dayOff
) {

	public static Store toStore(StoreUpdateReq request) {
		return Store.builder()
			.telephone(request.telephone)
			.address(AddressRes.toAddress(request.addressName, request.xCoordinate, request.yCoordinate))
			.openTime(request.openTime)
			.closeTime(request.closeTime)
			.dayOff(request.dayOff)
			.build();
	}
}
