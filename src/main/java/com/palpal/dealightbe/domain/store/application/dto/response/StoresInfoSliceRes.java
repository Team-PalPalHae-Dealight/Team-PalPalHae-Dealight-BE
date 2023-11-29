package com.palpal.dealightbe.domain.store.application.dto.response;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

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

	public static StoresInfoSliceRes of(List<StoreDocument> storeDocuments, Pageable pageable) {

		List<StoreInfoSliceRes> storesInfoSliceRes = storeDocuments.stream()
			.map(StoreInfoSliceRes::from)
			.toList();
		Slice<StoreDocument> storeDocumentSlice = new SliceImpl<>(storeDocuments, pageable, true);

		return new StoresInfoSliceRes(storesInfoSliceRes, storeDocumentSlice.hasNext());
	}
}
