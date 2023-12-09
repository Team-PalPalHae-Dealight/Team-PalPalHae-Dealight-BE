package com.palpal.dealightbe.domain.store.infrastructure;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.sort.NestedSortBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Repository;

import com.palpal.dealightbe.domain.item.domain.ItemDocument;
import com.palpal.dealightbe.domain.store.domain.StoreDocument;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;
import com.palpal.dealightbe.global.ListSortType;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StoreSearchRepositoryImpl {

	private final ElasticsearchOperations operations;
	private static final int BATCH_SIZE = 500;

	public void bulkInsertOrUpdate(List<StoreDocument> storeDocuments) {
		for (int i = 0; i < storeDocuments.size(); i += BATCH_SIZE) {
			int endIndex = Math.min(i + BATCH_SIZE, storeDocuments.size());
			List<StoreDocument> batch = storeDocuments.subList(i, endIndex);

			bulkIndexBatch(batch);
		}
	}

	private void bulkIndexBatch(List<StoreDocument> batch) {
		batch.forEach(storeDocument -> {
			Document document = operations.getElasticsearchConverter().mapObject(storeDocument);

			UpdateQuery updateQuery = UpdateQuery.builder(String.valueOf(storeDocument.getId()))
				.withDocument(document)
				.withDocAsUpsert(true)
				.build();

			operations.update(updateQuery, IndexCoordinates.of("store"));
		});
	}

	public void updateStoreItems(String storeId, List<ItemDocument> updatedItems) {
		List<Map<String, Object>> itemDocuments = updatedItems.stream()
			.map(item -> operations.getElasticsearchConverter().mapObject(item))
			.collect(Collectors.toList());

		UpdateQuery updateQuery = UpdateQuery.builder(storeId)
			.withDocument(Document.from(Collections.singletonMap("items", itemDocuments)))
			.build();

		operations.update(updateQuery, operations.getIndexCoordinatesFor(StoreDocument.class));
	}

	public void updateStoreStatus(String storeId, StoreStatus newStatus) {
		UpdateQuery updateQuery = UpdateQuery.builder(storeId)
			.withDocument(Document.from(Collections.singletonMap("storeStatus", newStatus)))
			.build();

		operations.update(updateQuery, operations.getIndexCoordinatesFor(StoreDocument.class));
	}

	public StoreDocument findById(String id) {
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
			.withQuery(QueryBuilders.matchQuery("id", id))
			.build();

		SearchHits<StoreDocument> searchHits = operations.search(searchQuery, StoreDocument.class);
		if (!searchHits.isEmpty()) {
			SearchHit<StoreDocument> searchHit = searchHits.getSearchHit(0);
			return searchHit.getContent();
		} else {
			return null;
		}
	}

	public Slice<StoreDocument> searchStores(double x, double y, String keyword, String sortBy, Pageable pageable) {
		NativeSearchQuery searchQuery = buildStoreSearchQuery(x, y, keyword, sortBy, pageable);
		SearchHits<StoreDocument> results = operations.search(searchQuery, StoreDocument.class);
		List<StoreDocument> storeDocuments = results.stream().map(SearchHit::getContent).toList();

		return toSlice(storeDocuments, pageable);
	}

	public NativeSearchQuery buildStoreSearchQuery(double x, double y, String keyword, String sortBy, Pageable pageable) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		// 거리 필터링
		QueryBuilder geoQuery = QueryBuilders.geoDistanceQuery("location")
			.point(y, x)
			.distance("3km");

		// 업체 상태 필터링
		QueryBuilder statusQuery = QueryBuilders.matchQuery("storeStatus", "OPENED");

		// Keyword 검색 (업체명 또는 아이템명)
		QueryBuilder keywordQuery = QueryBuilders.boolQuery()
			.should(QueryBuilders.matchQuery("name", keyword).operator(Operator.AND))
			.should(QueryBuilders.nestedQuery("items", QueryBuilders.matchQuery("items.name", keyword), ScoreMode.None));

		boolQuery.must(geoQuery);
		boolQuery.must(keywordQuery);
		boolQuery.must(statusQuery);

		SortBuilder<?> sortBuilder = getSortBuilder(x, y, sortBy);

		return new NativeSearchQueryBuilder()
			.withQuery(boolQuery)
			.withSorts(sortBuilder)
			.withPageable(pageable)
			.build();
	}

	private SortBuilder<?> getSortBuilder(double x, double y, String sortBy) {
		ListSortType sortType = ListSortType.findSortType(sortBy);

		switch (sortType) {
			case DISTANCE:
				return SortBuilders.geoDistanceSort("location", y, x)
					.point(y, x)
					.unit(DistanceUnit.KILOMETERS)
					.order(SortOrder.ASC);
			case DEADLINE:
				long currentTimeMillis = System.currentTimeMillis();

				String script = "Math.abs(doc['closeTime'].value.toInstant().toEpochMilli() - params.currentTimeMillis)";
				Map<String, Object> params = Collections.singletonMap("currentTimeMillis", currentTimeMillis);

				return SortBuilders.scriptSort(new Script(ScriptType.INLINE, "painless", script, params),
						ScriptSortBuilder.ScriptSortType.NUMBER)
					.order(SortOrder.DESC);
			case DISCOUNT_RATE:
				String nestedPath = "items";

				return SortBuilders.fieldSort("items.discountRate")
					.setNestedSort(new NestedSortBuilder(nestedPath))
					.order(SortOrder.DESC);
			default:
				return SortBuilders.geoDistanceSort("location", y, x)
					.point(y, x)
					.unit(DistanceUnit.KILOMETERS)
					.order(SortOrder.ASC);
		}
	}

	private Slice<StoreDocument> toSlice(List<StoreDocument> contents, Pageable pageable) {
		boolean hasNext = isContentSizeGreaterThanPageSize(contents, pageable);
		if (hasNext) {
			return new SliceImpl<>(subtractLastContent(contents, pageable), pageable, true);
		}
		return new SliceImpl<>(contents, pageable, false);
	}

	private boolean isContentSizeGreaterThanPageSize(List<StoreDocument> contents, Pageable pageable) {
		return pageable.isPaged() && contents.size() > pageable.getPageSize() - 1;
	}

	private List<StoreDocument> subtractLastContent(List<StoreDocument> content, Pageable pageable) {
		return content.subList(0, pageable.getPageSize() - 1);
	}
}
