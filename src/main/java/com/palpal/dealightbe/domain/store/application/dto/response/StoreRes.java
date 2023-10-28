package com.palpal.dealightbe.domain.store.application.dto.response;

import java.time.LocalDateTime;

import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.store.domain.Store;

public record StoreRes(
	String storeNumber,
	String name,
	String telephone,
	AddressRes addressRes,
	LocalDateTime openTime,
	LocalDateTime closeTime,
	String dayOff
) {

	public static StoreRes from(Store store) {
		return new StoreRes(
			store.getStoreNumber(), store.getName(), store.getTelephone(), AddressRes.from(store.getAddress()),
			store.getOpenTime(), store.getCloseTime(), store.getDayOff());
	}
}
