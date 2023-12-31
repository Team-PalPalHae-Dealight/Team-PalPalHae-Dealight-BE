package com.palpal.dealightbe.domain.member.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.global.BaseEntity;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@Entity
@Table(name = "members", indexes = {@Index(name = "index_providerId", columnList = "providerId")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "address_id")
	private Address address;

	private String realName;

	private String nickName;

	private String phoneNumber;

	private String provider;

	private Long providerId;

	private String image;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MemberRole> memberRoles = new ArrayList<>();

	@Builder
	public Member(String realName, String nickName, String phoneNumber, Address address, String provider,
		Long providerId) {
		this.realName = realName;
		this.nickName = nickName;
		this.phoneNumber = phoneNumber;
		this.address = getValidAddress(address);
		this.provider = provider;
		this.providerId = providerId;
	}

	private Address getValidAddress(Address address) {
		return address != null ? address : Address.defaultAddress();
	}

	public void updateInfo(Member member) {
		if (member == null) {
			log.warn("UPDATE_FAILED: Invalid member data provided.");
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		this.nickName = member.getNickName();
		this.phoneNumber = member.getPhoneNumber();
		updateAddress(member.getAddress());
	}

	public void updateAddress(Address address) {
		if (address == null) {
			log.warn("UPDATE_FAILED: Invalid address data provided.");
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		this.address.updateInfo(address);
	}

	public void updateImage(String imageUrl) {
		this.image = imageUrl;
	}

	public void updateMemberRoles(List<MemberRole> memberRoles) {
		if (memberRoles.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_ROLE_UPDATE);
		}

		if (!this.memberRoles.isEmpty()) {
			this.memberRoles.forEach(memberRole -> {
				memberRole.updateMember(null);
			});
			this.memberRoles.clear();
		}

		memberRoles.forEach(memberRole -> {
			this.memberRoles.add(memberRole);
			memberRole.updateMember(this);
		});
	}

	public boolean hasSameImage(String imageUrl) {
		return this.image != null && this.image.equals(imageUrl);
	}

	public boolean isRoleStore() {
		MemberRole memberRole = memberRoles.get(0);
		Role role = memberRole.getRole();

		return role.getType() == RoleType.ROLE_STORE;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("realName", realName)
			.append("nickName", nickName)
			.append("phoneNumber", phoneNumber)
			.append("provider", provider)
			.append("providerId", providerId)
			.toString();
	}
}
