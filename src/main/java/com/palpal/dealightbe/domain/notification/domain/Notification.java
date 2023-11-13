package com.palpal.dealightbe.domain.notification.domain;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderStatus;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.BaseEntity;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id")
	private Store store;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	@Enumerated(EnumType.STRING)
	private NotificationType type;

	private String message;

	private boolean isRead = false;

	@Builder
	public Notification(Member member, Store store, Order order, NotificationType type, String message) {
		this.member = member;
		this.store = store;
		this.order = order;
		this.type = type;
		this.message = message;
	}

	public void markAsRead() {
		this.isRead = true;
	}

	public enum NotificationType {
		ORDER_RECEIVED,
		ORDER_CONFIRMED,
		ORDER_COMPLETED,
		ORDER_CANCELED
	}

	public static String createMessage(OrderStatus orderStatus, Order order) {
		switch (orderStatus) {
			case RECEIVED:
				return "새 주문이 도착했습니다: " + order.getId();
			case CONFIRMED:
				return "주문이 수락되었습니다: " + order.getId();
			case COMPLETED:
				return "주문이 완료되었습니다: " + order.getId();
			case CANCELED:
				return "주문이 취소되었습니다: " + order.getId();
			default:
				throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
		}
	}
}
