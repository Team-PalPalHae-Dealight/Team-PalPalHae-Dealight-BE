package com.palpal.dealightbe.domain.member.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.palpal.dealightbe.domain.address.domain.Address;
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

	private String realName;

	private String nickName;

	private String phoneNumber;

	private boolean isDeleted = false;

	private String provider;

	private String providerId;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "member")
	private List<MemberRole> memberRoles = new ArrayList<>();

	@Builder
	public Member(String realName, String nickName, String phoneNumber) {
		this.realName = realName;
		this.nickName = nickName;
		this.phoneNumber = phoneNumber;
	}

	public void setAddress(Address address) {
		this.address = address;
	}
}
