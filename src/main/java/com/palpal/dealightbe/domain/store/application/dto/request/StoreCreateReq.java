package com.palpal.dealightbe.domain.store.application.dto.request;

import java.time.LocalTime;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;

public record StoreCreateReq(
	@NotBlank(message = "사업자 등록 번호는 필수 입력값입니다.")
	@Pattern(regexp = "\\d+", message = "숫자만 입력해 주세요.")
	String storeNumber,

	@NotBlank(message = "상호명은 필수 입력값입니다.")
	String name,

	@NotBlank(message = "업체 전화번호는 필수 입력값입니다.")
	@Pattern(regexp = "\\d+", message = "숫자만 입력해 주세요.")
	String telephone,

	@NotBlank(message = "업체 주소는 필수 입력값입니다.")
	String addressName,

	double xCoordinate,
	double yCoordinate,
	LocalTime openTime,
	LocalTime closeTime,
	Set<DayOff> dayOff
) {

	public static Store toStore(StoreCreateReq request, Address address, Member member) {
		return Store.builder()
			.member(member)
			.storeNumber(request.storeNumber)
			.name(request.name)
			.address(address)
			.telephone(request.telephone)
			.openTime(request.openTime)
			.closeTime(request.closeTime)
			.dayOff(request.dayOff)
			.build();
	}
}
