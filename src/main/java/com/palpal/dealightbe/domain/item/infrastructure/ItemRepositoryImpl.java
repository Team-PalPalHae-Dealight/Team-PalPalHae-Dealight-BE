package com.palpal.dealightbe.domain.item.infrastructure;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.item.domain.ItemRepositoryCustom;
import com.palpal.dealightbe.domain.item.domain.QItem;
import com.querydsl.jpa.impl.JPAQueryFactory;

@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private QItem item = QItem.item;

	@Override
	public Page<Item> findAllByStoreIdOrderByUpdatedAtDesc(Long storeId, Pageable pageable) {
		List<Item> items = queryFactory.select(item)
			.from(item)
			.where(item.store.id.eq(storeId))
			.orderBy(item.updatedAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long count = queryFactory.select(item.count())
			.from(item)
			.where(item.store.id.eq(storeId))
			.fetchOne();

		return new PageImpl<>(items, pageable, count);
	}
}
