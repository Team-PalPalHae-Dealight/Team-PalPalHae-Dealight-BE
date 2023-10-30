package com.palpal.dealightbe.domain.store.domain;

import java.time.LocalTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
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
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@Entity
@Table(name = "stores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class Store extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "member_id")
	private Member member;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "address_id")
	private Address address;

	private String name;

	@Enumerated(EnumType.STRING)
	private StoreStatus storeStatus = StoreStatus.CLOSED;

	private String storeNumber;

	private String telephone;

	private LocalTime openTime;

	private LocalTime closeTime;

	private String image;

	@ElementCollection(targetClass = DayOff.class)
	@CollectionTable(name = "store_day_off", joinColumns = @JoinColumn(name = "store_id"))
	@Enumerated(EnumType.STRING)
	private Set<DayOff> dayOffs;

	@Builder
	public Store(Address address, String name, String storeNumber, String telephone, LocalTime openTime, LocalTime closeTime, Set<DayOff> dayOff) {
		validateBusinessTimes(openTime, closeTime);
		this.address = address;
		this.name = name;
		this.storeNumber = storeNumber;
		this.telephone = telephone;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.dayOffs = dayOff;
	}

	public void updateMember(Member member) {
		this.member = member;
	}

	public void updateAddress(Address address) {
		this.address = address;
	}

	public void updateImage(String image) {
		this.image = image;
	}

	private void validateBusinessTimes(LocalTime openTime, LocalTime closeTime) {
		if (closeTime.isBefore(openTime)) {
			log.warn("INVALID_BUSINESS_TIME : openTime => {}, closeTime => {}", openTime, closeTime);
			throw new BusinessException(ErrorCode.INVALID_BUSINESS_TIME);
		}
	}
}
