package com.palpal.dealightbe.domain.store.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.item.domain.Item;
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

	private static final String DEFAULT_PATH = "https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/image/free-store-icon.png";

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

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "store")
	private List<Item> items = new ArrayList<>();

	@Builder
	public Store(Member member, Address address, String name, String storeNumber, String telephone, LocalTime openTime,
		LocalTime closeTime, Set<DayOff> dayOff) {
		validateBusinessTimes(openTime, closeTime);
		this.member = member;
		this.address = address;
		this.name = name;
		this.storeNumber = storeNumber;
		this.telephone = telephone;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.dayOffs = dayOff;
		this.image = DEFAULT_PATH;
	}

	public void updateMember(Member member) {
		this.member = member;
	}

	public void updateInfo(Store store) {
		this.telephone = store.getTelephone();
		this.address = store.getAddress();
		this.openTime = store.getOpenTime();
		this.closeTime = store.getCloseTime();
		this.dayOffs = store.getDayOffs();
	}

	public void updateImage(String image) {
		this.image = image;
	}

	public void updateAddress(Address address) {
		this.address = address;
	}

	public void isSameOwnerAndTheRequester(Member member, Store store) {
		Long ownerId = store.getMember().getProviderId();
		Long requesterId = member.getProviderId();

		if (!(ownerId == requesterId)) {
			log.warn("GET:READ:NOT_MATCH_OWNER_AND_REQUESTER : ownerId => {} memberId => {}", ownerId, requesterId);
			throw new BusinessException(ErrorCode.NOT_MATCH_OWNER_AND_REQUESTER);
		}
	}

	public boolean isSameOwnerAndTheRequester(long requesterId) {
		long ownerId = getMember().getProviderId();

		return (ownerId == requesterId);
	}

	public void updateStatus(StoreStatus storeStatus) {
		this.storeStatus = storeStatus;
	}

	public void addItem(Item item) {
		this.items.add(item);
	}

	private void validateBusinessTimes(LocalTime openTime, LocalTime closeTime) {
		if (openTime.isAfter(closeTime)) {
			if (openTime.isAfter(LocalTime.of(0, 0)) && closeTime.isBefore(LocalTime.of(5, 0))) {
				return;
			}
			log.warn("INVALID_BUSINESS_TIME : openTime => {}, closeTime => {}", openTime, closeTime);
			throw new BusinessException(ErrorCode.INVALID_BUSINESS_TIME);
		}
	}
}
