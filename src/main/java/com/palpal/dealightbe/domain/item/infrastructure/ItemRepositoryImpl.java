package com.palpal.dealightbe.domain.item.infrastructure;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.item.domain.ItemRepositoryCustom;
import com.palpal.dealightbe.domain.item.domain.QItem;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private QItem item = QItem.item;

	@Override
	public Slice<Item> findAllByStoreIdOrderByUpdatedAtDesc(Long storeId, Pageable pageable) {
		List<Item> items = queryFactory.select(item)
			.from(item)
			.where(item.store.id.eq(storeId))
			.orderBy(item.updatedAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = false;
		if (items.size() > pageable.getPageSize()) {
			items.remove(pageable.getPageSize());
			hasNext = true;
		}

		return new SliceImpl<>(items, pageable, hasNext);
	}
}
