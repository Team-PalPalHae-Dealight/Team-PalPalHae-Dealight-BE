package com.palpal.dealightbe.domain.store.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.global.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "stores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "address_id")
	private Address address;

	private String name;

	@Enumerated(EnumType.STRING)
	private StoreStatus storeStatus = StoreStatus.CLOSED;

	private String storeNumber;

	private String telephone;

	private LocalDateTime openTime;

	private LocalDateTime closeTime;

	private String image;

	private String dayOff;

	private boolean isDeleted = false;

	@Builder
	public Store(Address address, String name, String storeNumber, String telephone, LocalDateTime openTime, LocalDateTime closeTime, String dayOff) {
		this.address = address;
		this.name = name;
		this.storeNumber = storeNumber;
		this.telephone = telephone;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.dayOff = dayOff;
	}

	public void updateMember(Member member) {
		this.member = member;
	}

	public void updateAddress(Address address) {
		this.address = address;
	}
}
