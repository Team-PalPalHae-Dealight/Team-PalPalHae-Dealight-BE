package com.palpal.dealightbe.domain.image.dto.response;

public record ImageRes(
	String imageUrl
) {

	public static ImageRes from(String imageUrl) {
		return new ImageRes(imageUrl);
	}
}
