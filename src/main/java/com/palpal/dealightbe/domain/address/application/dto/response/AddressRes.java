package com.palpal.dealightbe.domain.address.application.dto.response;

import com.palpal.dealightbe.domain.address.domain.Address;

public record AddressRes(
	String name,
	double xCoordinate,
	double yCoordinate
) {

	public static AddressRes from(Address address) {
		return new AddressRes(address.getName(), address.getXCoordinate(), address.getYCoordinate());
	}

	public static Address toAddress(AddressRes addressRes) {
		return Address.builder()
			.name(addressRes.name)
			.xCoordinate(addressRes.xCoordinate)
			.yCoordinate(addressRes.yCoordinate)
			.build();
	}
}
