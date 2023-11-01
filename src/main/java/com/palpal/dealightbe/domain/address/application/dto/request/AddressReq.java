package com.palpal.dealightbe.domain.address.application.dto.request;

public record AddressReq(
	String name,
	double xCoordinate,
	double yCoordinate
) {
}
