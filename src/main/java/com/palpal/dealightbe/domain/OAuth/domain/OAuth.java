package com.palpal.dealightbe.domain.OAuth.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.palpal.dealightbe.global.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "oauth")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String provider;

	private Long providerId;

	private String refreshToken;

	@Builder
	public OAuth(String provider, Long providerId, String refreshToken) {
		this.provider = provider;
		this.providerId = providerId;
		this.refreshToken = refreshToken;
	}
}
