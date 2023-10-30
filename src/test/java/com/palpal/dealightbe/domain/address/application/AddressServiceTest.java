package com.palpal.dealightbe.domain.address.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.address.domain.AddressRepository;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

	@Mock
	AddressRepository addressRepository;

	@InjectMocks
	AddressService addressService;

	@Test
	@DisplayName("주소 저장 성공 테스트")
	void registerAddressSuccessTest() throws Exception {

		// given
		String name = "서울시 강남구";
		double x = 67.89;
		double y = 293.2323;

		Address addressToSave = Address.builder()
			.name(name)
			.xCoordinate(x)
			.yCoordinate(y)
			.build();

		when(addressRepository.save(any(Address.class))).thenReturn(addressToSave);

		// when
		AddressRes addressRes = addressService.register(name, x, y);

		// then
		assertNotNull(addressRes);
		assertEquals(name, addressRes.name());
		assertEquals(x, addressRes.xCoordinate());
		assertEquals(y, addressRes.yCoordinate());
	}
}
