package com.palpal.dealightbe.domain.order.domain;

import static com.palpal.dealightbe.domain.order.domain.OrderStatus.CANCELED;
import static com.palpal.dealightbe.domain.order.domain.OrderStatus.COMPLETED;
import static com.palpal.dealightbe.domain.order.domain.OrderStatus.RECEIVED;
import static com.palpal.dealightbe.global.error.ErrorCode.EXCEEDED_ORDER_ITEMS;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_ARRIVAL_TIME;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_DEMAND_LENGTH;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_ORDER_STATUS;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_ORDER_STATUS_UPDATER;
import static com.palpal.dealightbe.global.error.ErrorCode.UNCHANGEABLE_ORDER_STATUS;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.util.Pair;

import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.BaseEntity;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class Order extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id")
	private Store store;

	private LocalTime arrivalTime;

	private String demand;

	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus = RECEIVED;

	@OneToMany(mappedBy = "order")
	private List<OrderItem> orderItems = new ArrayList<>();

	private int totalPrice;

	private static final int MAX_DEMAND_LENGTH = 100;

	private static final int MAX_ORDER_ITEMS = 5;

	private static final Set<OrderStatus> UNCHANGEABLE_STATUS = Set.of(COMPLETED, CANCELED);

	private static final Set<Pair<String, String>> orderStatusSequenceOfMember = Set.of(
		Pair.of("RECEIVED", "CANCELED"),
		Pair.of("CONFIRMED", "CANCELED")
	);

	private static final Set<Pair<String, String>> orderStatusSequenceOfStore = Set.of(
		Pair.of("RECEIVED", "CONFIRMED"),
		Pair.of("RECEIVED", "CANCELED"),
		Pair.of("CONFIRMED", "COMPLETED"),
		Pair.of("CONFIRMED", "CANCELED")
	);

	@Builder
	public Order(Member member, Store store, LocalTime arrivalTime, String demand, int totalPrice) {
		validateArrivalTime(store, arrivalTime);
		validateDemand(demand);
		this.member = member;
		this.store = store;
		this.arrivalTime = arrivalTime;
		this.demand = demand;
		this.totalPrice = totalPrice;
	}

	public void addOrderItems(List<OrderItem> orderItems) {
		if (orderItems.size() > MAX_ORDER_ITEMS) {
			log.warn("POST:WRITE:EXCEEDED_ORDER_ITEMS : {}", orderItems.size());
			throw new BusinessException(EXCEEDED_ORDER_ITEMS);
		}

		this.orderItems = orderItems;
	}

	public void validateOrderUpdater(Member updater) {
		Long storeOwnerId = store.getMember().getProviderId();
		Long memberId = member.getProviderId();
		Long updaterId = updater.getProviderId();

		if (!(updaterId.equals(storeOwnerId) || updaterId.equals(memberId))) {
			log.warn("GET:READ:NO_AUTHORITY_TO_UPDATE_ORDER_STATUS: STORE_OWNER{} MEMBER{} UPDATER{}", storeOwnerId,
				memberId, updaterId);
			throw new BusinessException(INVALID_ORDER_STATUS_UPDATER);
		}
	}

	public void changeStatus(Member updater, String originalStatus, String changedStatus) {
		validateStatusRequest(changedStatus);
		validateOrderUpdater(updater);
		validateUpdaterAuthority(updater, originalStatus, changedStatus);

		this.orderStatus = OrderStatus.valueOf(changedStatus);
	}

	public void validateStatusRequest(String changedStatus) {
		if (!OrderStatus.isValidStatus(changedStatus)) {
			log.warn("GET:READ:NOT_EXISTED_ORDER_STATUS: {}", changedStatus);
			throw new BusinessException(INVALID_ORDER_STATUS);
		}

		OrderStatus currentStatus = OrderStatus.valueOf(orderStatus.name());

		if (isUnchangeableStatus(currentStatus)) {
			log.warn("GET:READ:CAN_NOT_CHANGE_STATUS: CURRENT_STATUS{}", currentStatus.getText());
			throw new BusinessException(UNCHANGEABLE_ORDER_STATUS);
		}
	}

	private void validateDemand(String demand) {
		if (demand.length() > MAX_DEMAND_LENGTH) {
			log.warn("POST:WRITE:TOO_LONG_DEMAND:LENGTH {}", demand.length());
			throw new BusinessException(INVALID_DEMAND_LENGTH);
		}
	}

	private void validateArrivalTime(Store store, LocalTime arrivalTime) {
		LocalTime storeCloseTime = store.getCloseTime();
		LocalTime storeOpenTime = store.getOpenTime();

		if (storeCloseTime.isBefore(storeOpenTime)) {
			storeCloseTime = storeCloseTime.plusHours(24);
		}

		if (arrivalTime.isBefore(storeOpenTime) && arrivalTime.isAfter(storeCloseTime)) {
			log.warn("POST:WRITE:INVALID_ARRIVAL_TIME:STORE_OPEN {}, STORE_CLOSE {}, ARRIVAL_TIME {}", storeOpenTime,
				storeCloseTime, arrivalTime);
			throw new BusinessException(INVALID_ARRIVAL_TIME);
		}
	}

	private boolean isUnchangeableStatus(OrderStatus status) {
		return UNCHANGEABLE_STATUS.contains(status);
	}

	private void validateUpdaterAuthority(Member updater, String originalStatus, String changedStatus) {
		Pair<String, String> inputSequence = Pair.of(originalStatus, changedStatus);

		if (isMember(updater) && !orderStatusSequenceOfMember.contains(inputSequence)) {
			log.warn("PATCH:UPDATE:MEMBER:CANNOT_CHANGE_STATUS:{} -> {}", originalStatus, changedStatus);
			throw new BusinessException(INVALID_ORDER_STATUS);
		}
		if (isStoreOwner(updater) && !orderStatusSequenceOfStore.contains(inputSequence)) {
			log.warn("PATCH:UPDATE:STORE:CANNOT_CHANGE_STATUS:{} -> {}", originalStatus, changedStatus);
			throw new BusinessException(INVALID_ORDER_STATUS);
		}
	}

	private boolean isMember(Member updater) {
		long updaterId = updater.getProviderId();
		long orderedMemberId = this.member.getProviderId();

		return updaterId == orderedMemberId;
	}

	private boolean isStoreOwner(Member updater) {
		long updaterId = updater.getProviderId();
		long storeOwnerId = store.getMember().getProviderId();

		return updaterId == storeOwnerId;
	}
}
