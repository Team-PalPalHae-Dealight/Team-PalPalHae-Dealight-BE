package com.palpal.dealightbe.domain.address.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.address.domain.AddressRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class AddressService {

	private final AddressRepository addressRepository;

	public AddressRes register(String name, double x, double y) {
		Address address = Address.builder()
			.name(name)
			.xCoordinate(x)
			.yCoordinate(y)
			.build();

		addressRepository.save(address);

		return AddressRes.from(address);
	}
}
