package com.palpal.dealightbe.domain.order.domain;

import static com.palpal.dealightbe.domain.order.domain.OrderStatus.RECEIVED;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_ARRIVAL_TIME;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_DEMAND_LENGTH;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
import javax.validation.constraints.Size;

import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.BaseEntity;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
	@Size(min = 1, max = 5, message = "한 번에 5개 종류의 상품까지만 주문할 수 있습니다.")
	private List<OrderItem> orderItems = new ArrayList<>();

	private int totalPrice;

	private static final int MAX_DEMAND_LENGTH = 100;

	@Builder
	public Order(Member member, Store store, LocalTime arrivalTime, String demand, int totalPrice) {
		validateArrivalTime(arrivalTime);
		validateDemand(demand);
		this.member = member;
		this.store = store;
		this.arrivalTime = arrivalTime;
		this.demand = demand;
		this.totalPrice = totalPrice;
	}

	private void validateDemand(String demand) {
		if (demand.length() > MAX_DEMAND_LENGTH) {
			throw new BusinessException(INVALID_DEMAND_LENGTH);
		}
	}

	private void validateArrivalTime(LocalTime arrivalTime) {
		if (store.getCloseTime().isBefore(arrivalTime)) {
			throw new BusinessException(INVALID_ARRIVAL_TIME);
		}
	}

	public void addOrderItems(List<OrderItem> orderItems) {
		this.orderItems = orderItems;
	}
}
