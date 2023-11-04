package com.palpal.dealightbe.domain.address.application.dto.request;

import com.palpal.dealightbe.domain.address.domain.Address;

public record AddressReq(
	String name,
	double xCoordinate,
	double yCoordinate
) {

	public static Address toAddress(AddressReq addressReq) {
		return new Address(addressReq.name(), addressReq.xCoordinate(), addressReq.yCoordinate());
	}
}
