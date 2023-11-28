package com.palpal.dealightbe.domain.store.application.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreDocument;

public record StoresInfoSliceRes(
	List<StoreInfoSliceRes> storeInfoSliceRes,
	boolean hasNext
) {

	public static StoresInfoSliceRes from(Slice<Store> storeSlice) {
		List<StoreInfoSliceRes> storesInfoSliceRes = storeSlice.stream()
			.map(StoreInfoSliceRes::from)
			.toList();

		return new StoresInfoSliceRes(storesInfoSliceRes, storeSlice.hasNext());
	}

	public static StoresInfoSliceRes fromDocuments(Slice<StoreDocument> storeDocuments) {

		List<StoreInfoSliceRes> storesInfoSliceRes = storeDocuments.stream()
			.map(StoreInfoSliceRes::from)
			.toList();

		return new StoresInfoSliceRes(storesInfoSliceRes, storeDocuments.hasNext());
	}
}
