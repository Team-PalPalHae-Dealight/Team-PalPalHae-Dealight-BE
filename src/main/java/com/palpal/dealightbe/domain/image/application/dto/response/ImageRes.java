package com.palpal.dealightbe.domain.image.application.dto.response;

import com.palpal.dealightbe.domain.store.domain.Store;

public record ImageRes(
	String imageUrl
) {
	public static ImageRes from(Store store) {
		return new ImageRes(store.getImage());
	}
}
