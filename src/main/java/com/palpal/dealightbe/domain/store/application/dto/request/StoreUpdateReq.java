package com.palpal.dealightbe.domain.store.application.dto.request;

import java.time.LocalTime;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;

public record StoreUpdateReq(
	@NotBlank(message = "업체 전화번호는 필수 입력값입니다.")
	@Pattern(regexp = "\\d+")
	String telephone,

	@NotBlank(message = "업체 주소는 필수 입력값입니다.")
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
