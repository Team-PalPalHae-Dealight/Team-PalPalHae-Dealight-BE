package com.palpal.dealightbe.domain.store.application.dto.request;

import java.time.LocalTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.store.domain.Store;

public record StoreCreateReq(
	@NotBlank(message = "사업자 등록 번호는 필수 입력값입니다.")
	String storeNumber,

	@NotBlank(message = "상호명은 필수 입력값입니다.")
	String name,

	@NotBlank(message = "업체 전화번호는 필수 입력값입니다.")
	@Pattern(regexp = "\\d+")
	String telephone,

	@NotBlank(message = "업체 주소는 필수 입력값입니다.")
	String addressName,

	double xCoordinate,
	double yCoordinate,
	LocalTime openTime,
	LocalTime closeTime,
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
