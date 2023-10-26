package com.palpal.dealightbe.domain.member.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.oAuth.domain.OAuth;
import com.palpal.dealightbe.global.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "address_id")
	private Address address;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "oauth_id")
	private OAuth oAuth;

	private String realName;

	private String nickName;

	private String phoneNumber;

	private boolean isDeleted = false;

	@Builder
	public Member(OAuth oAuth, String realName, String nickName, String phoneNumber) {
		this.oAuth = oAuth;
		this.realName = realName;
		this.nickName = nickName;
		this.phoneNumber = phoneNumber;
	}
}
