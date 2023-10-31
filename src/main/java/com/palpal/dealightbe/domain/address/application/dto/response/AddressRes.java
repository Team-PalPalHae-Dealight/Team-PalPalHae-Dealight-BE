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

	public static Address toAddress(String name, double xCoordinate, double yCoordinate) {
		return Address.builder()
			.name(name)
			.xCoordinate(xCoordinate)
			.yCoordinate(yCoordinate)
			.build();
	}
}
