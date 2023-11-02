package com.palpal.dealightbe.domain.member.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

	private boolean isDeleted = false;

	private String provider;

	private Long providerId;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.ALL)
	private List<MemberRole> memberRoles = new ArrayList<>();

	@Builder
	public Member(String realName, String nickName, String phoneNumber, String provider, Long providerId,
		List<MemberRole> memberRoles) {
		this.realName = realName;
		this.nickName = nickName;
		this.phoneNumber = phoneNumber;
		this.address = Address.defaultAddress();
		this.provider = provider;
		this.providerId = providerId;
		this.memberRoles = memberRoles;
	}

	public void updateInfo(String nickName, String phoneNumber, Address address) {
		this.nickName = nickName;
		this.phoneNumber = phoneNumber;
		updateAddress(address);
	}

	public void updateAddress(Address address) {
		this.address.updateInfo(address.getName(), address.getXCoordinate(), address.getYCoordinate());
	}

	public void updateMemberRoles(List<MemberRole> memberRoles) {
		if (memberRoles.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_ROLE_UPDATE);
		}

		this.memberRoles = memberRoles;
		memberRoles.forEach(memberRole -> {
			memberRole.updateMember(this);
		});
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("realName", realName)
			.append("nickName", nickName)
			.append("phoneNumber", phoneNumber)
			.append("isDeleted", isDeleted)
			.append("provider", provider)
			.append("providerId", providerId)
			.toString();
	}
}
