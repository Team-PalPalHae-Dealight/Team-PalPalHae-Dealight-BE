package com.palpal.dealightbe.domain.item.domain;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.palpal.dealightbe.domain.address.domain.QAddress;
import com.palpal.dealightbe.domain.store.domain.QStore;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;
import com.palpal.dealightbe.global.ListSortType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom {

	private static final String HAVERSINE = "(6371 * ACOS(COS(RADIANS({0})) * COS(RADIANS({1}.yCoordinate)) * COS(RADIANS({1}.xCoordinate) - RADIANS({2})) + SIN(RADIANS({0})) * SIN(RADIANS({1}.yCoordinate))))";
	private static final String DEADLINE = "CASE WHEN {0} < CURTIME() THEN (24 * 60 * 60 + (HOUR({0}) * 3600 + MINUTE({0}) * 60 + SECOND({0}))) ELSE (HOUR({0}) * 3600 + MINUTE({0}) * 60 + SECOND({0})) END";
	private static final String DISCOUNT_RATE = "(item.originalPrice - item.discountPrice) * 1.0 / item.originalPrice";

	private final JPAQueryFactory queryFactory;
	private QItem item = QItem.item;
	private QStore store = QStore.store;
	private QAddress address = QAddress.address;

	@Override
	public Slice<Item> findAllByStoreIdOrderByUpdatedAtDesc(Long storeId, Pageable pageable) {
		List<Item> result = queryFactory
			.selectFrom(item)
			.join(item.store, store).fetchJoin()
			.join(store.address, address).fetchJoin()
			.where(item.store.id.eq(storeId))
			.orderBy(item.updatedAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		return checkLastPage(pageable, result);
	}

	@Override
	public Slice<Item> findAllByOpenedStatusAndDistanceWithin3KmAndSortCondition(double xCoordinate, double yCoordinate, String sortBy, Pageable pageable) {
		BooleanExpression distancePredicate = getDistancePredicate(xCoordinate, yCoordinate);

		OrderSpecifier[] orderSpecifiers = orderSpecifiers(xCoordinate, yCoordinate, sortBy);

		List<Item> result = queryFactory
			.selectFrom(item)
			.join(item.store, store).fetchJoin()
			.join(store.address, address).fetchJoin()
			.where(store.storeStatus.eq(StoreStatus.OPENED),
				distancePredicate)
			.orderBy(orderSpecifiers)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		return checkLastPage(pageable, result);
	}

	private OrderSpecifier[] orderSpecifiers(double xCoordinate, double yCoordinate, String sortBy) {
		ListSortType sortType = ListSortType.findSortType(sortBy);

		switch (sortType) {
			case DISTANCE -> {
				return createOrderSpecifier(getDistanceWithin3KmExpression(xCoordinate, yCoordinate).asc());
			}

			case DISCOUNT_RATE -> {
				return createOrderSpecifier(getDiscountRateExpression().desc());
			}

			case DEADLINE -> {
				return createOrderSpecifier(getDeadlineExpression().asc());
			}
		}

		return createOrderSpecifier(getDistanceWithin3KmExpression(xCoordinate, yCoordinate).asc());
	}

	private BooleanTemplate getDistancePredicate(double xCoordinate, double yCoordinate) {

		return Expressions.booleanTemplate(HAVERSINE + " <= 3", yCoordinate, address, xCoordinate);
	}

	private OrderSpecifier[] createOrderSpecifier(OrderSpecifier<Double> orderSpecifier) {

		return new OrderSpecifier[] {orderSpecifier, item.updatedAt.desc()};
	}

	private NumberTemplate<Double> getDistanceWithin3KmExpression(double xCoordinate, double yCoordinate) {

		return Expressions.numberTemplate(Double.class, HAVERSINE, yCoordinate, address, xCoordinate);
	}

	private NumberTemplate<Double> getDiscountRateExpression() {

		return Expressions.numberTemplate(Double.class, DISCOUNT_RATE);
	}

	private NumberTemplate<Double> getDeadlineExpression() {

		return Expressions.numberTemplate(Double.class, DEADLINE, store.closeTime);
	}

	private Slice<Item> checkLastPage(Pageable pageable, List<Item> results) {
		boolean hasNext = false;

		if (results.size() > pageable.getPageSize()) {
			hasNext = true;
			results.remove(pageable.getPageSize());
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}
}
