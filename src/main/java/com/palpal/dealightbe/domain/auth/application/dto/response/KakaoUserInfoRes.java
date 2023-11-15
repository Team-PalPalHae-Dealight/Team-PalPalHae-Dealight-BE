package com.palpal.dealightbe.domain.auth.application.dto.response;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("profileNicknameNeedsAgreement", profileNicknameNeedsAgreement)
				.append("profileImageNeedsAgreement", profileImageNeedsAgreement)
				.append("profile", profile)
				.toString();
		}
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

		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("nickname", nickname)
				.append("thumbnailImageUrl", thumbnailImageUrl)
				.append("profileImageUrl", profileImageUrl)
				.append("isDefaultImage", isDefaultImage)
				.toString();
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("id", id)
			.append("connectedAt", connectedAt)
			.append("properties", properties)
			.append("kakaoAccount", kakaoAccount)
			.toString();
	}
}
