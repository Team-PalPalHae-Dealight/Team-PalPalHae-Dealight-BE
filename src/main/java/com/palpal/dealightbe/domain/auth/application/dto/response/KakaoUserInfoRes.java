package com.palpal.dealightbe.domain.auth.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoRes(
	long id,
	@JsonProperty("connected_at")
	String connectedAt,
	Properties properties,
	@JsonProperty("kakao_account")
	KakaoAccount kakaoAccount
) {

	public record Properties(
		String nickname,
		@JsonProperty("profile_image")
		String profileImage,
		@JsonProperty("thumbnail_image")
		String thumbnailImage
	) {
	}

	public record KakaoAccount(
		@JsonProperty("profile_nickname_needs_agreement")
		boolean profileNicknameNeedsAgreement,
		@JsonProperty("profile_image_needs_agreement")
		boolean profileImageNeedsAgreement,
		Profile profile
	) {
	}

	public record Profile(
		String nickname,
		@JsonProperty("thumbnail_image_url")
		String thumbnailImageUrl,
		@JsonProperty("profile_image_url")
		String profileImageUrl,
		@JsonProperty("is_default_image")
		boolean isDefaultImage
	) {
	}
}
