package com.palpal.dealightbe.domain.store.domain;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.palpal.dealightbe.domain.address.domain.QAddress;
import com.palpal.dealightbe.domain.item.domain.QItem;
import com.palpal.dealightbe.global.ListSortType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private QStore store = QStore.store;
	private QItem item = QItem.item;
	private QAddress address = QAddress.address;
	private final static String HAVERSINE = "(6371 * ACOS(COS(RADIANS({0})) * COS(RADIANS({1}.yCoordinate)) * COS(RADIANS({1}.xCoordinate) - RADIANS({2})) + SIN(RADIANS({0})) * SIN(RADIANS({1}.yCoordinate))))";
	private final static String DEADLINE = "ABS(EXTRACT(HOUR FROM {0}) * 60 + EXTRACT(MINUTE FROM {0}) - " +
		"(EXTRACT(HOUR FROM CURRENT_TIME) * 60 + EXTRACT(MINUTE FROM CURRENT_TIME)))";
	private final static String DISCOUNT_RATE = "(item.originalPrice - item.discountPrice) * 1.0 / item.originalPrice";

	@Override
	public Slice<Store> findByKeywordAndDistanceWithin3KmAndSortCondition(double xCoordinate, double yCoordinate, String keyword, String sortBy, Long cursor, Pageable pageable) {
		BooleanExpression distancePredicate = getDistanceWithin3KmPredicate(xCoordinate, yCoordinate);

		BooleanExpression keywordPredicate = getKeywordPredicate(keyword);

		orderSpecifiers(xCoordinate, yCoordinate, sortBy);

		List<Store> result = queryFactory
			.selectFrom(store)
			.join(store.address, address)
			.leftJoin(item)
			.on(store.eq(item.store)
				.and(item.store.id.eq(store.id)))
			.where(store.storeStatus.eq(StoreStatus.OPENED),
				distancePredicate,
				keywordPredicate,
				ltStoreId(cursor))
			.orderBy(orderSpecifiers(xCoordinate, yCoordinate, sortBy))
			.limit(pageable.getPageSize() + 1)
			.fetch().stream().distinct().collect(Collectors.toList());

		return checkLastPage(pageable, result);
	}

	private OrderSpecifier[] orderSpecifiers(double xCoordinate, double yCoordinate, String sortBy) {
		ListSortType sortType = ListSortType.findSortType(sortBy);
		OrderSpecifier<?>[] orderSpecifiers;

		switch (sortType) {
			case DISTANCE:
				orderSpecifiers = new OrderSpecifier[]{getDistanceByNear(xCoordinate, yCoordinate).asc(), item.updatedAt.desc()};
				break;
			case DISCOUNT_RATE:
				orderSpecifiers = new OrderSpecifier[]{getBigDiscountRate().desc(), item.updatedAt.desc()};
				break;
			case DEADLINE:
				orderSpecifiers = new OrderSpecifier[]{getDeadlineImminent().asc(), item.updatedAt.desc()};
				break;
			default:
				orderSpecifiers = new OrderSpecifier[]{getDistanceByNear(xCoordinate, yCoordinate).asc(), item.updatedAt.desc()};
				break;
		}
		return orderSpecifiers;
	}

	private BooleanExpression ltStoreId(Long storeId) {
		if (storeId == null) {
			return null;
		}

		return store.id.gt(storeId);
	}

	private BooleanTemplate getDistanceWithin3KmPredicate(double xCoordinate, double yCoordinate) {
		return Expressions.booleanTemplate(HAVERSINE + " <= 3", yCoordinate, address, xCoordinate);
	}

	private BooleanExpression getKeywordPredicate(String keyword) {
		BooleanExpression keywordPredicate = store.name.like("%" + keyword + "%")
			.or(item.name.like("%" + keyword + "%"));
		return keywordPredicate;
	}

	private Slice<Store> checkLastPage(Pageable pageable, List<Store> resultList) {
		boolean hasNext = false;

		if (resultList.size() > pageable.getPageSize()) {
			hasNext = true;
			resultList.remove(pageable.getPageSize());
		}

		return new SliceImpl<>(resultList, pageable, hasNext);
	}

	private NumberTemplate<Double> getDeadlineImminent() {
		return Expressions.numberTemplate(Double.class, DEADLINE, store.closeTime);
	}

	private static NumberTemplate<Double> getBigDiscountRate() {
		return Expressions.numberTemplate(Double.class, DISCOUNT_RATE);
	}

	private NumberTemplate<Double> getDistanceByNear(double xCoordinate, double yCoordinate) {
		return Expressions.numberTemplate(Double.class, HAVERSINE, yCoordinate, address, xCoordinate);
	}
}
