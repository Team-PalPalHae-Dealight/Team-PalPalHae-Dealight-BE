package com.palpal.dealightbe.domain.store.infrastructure;

import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Repository;

import com.palpal.dealightbe.domain.store.domain.StoreDocument;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StoreSearchRepositoryImpl {

	private final ElasticsearchOperations operations;

	public void bulkInsertOrUpdate(List<StoreDocument> storeDocuments) {
		List<UpdateQuery> updateQueries = storeDocuments.stream().map(storeDocument ->
			UpdateQuery.builder(String.valueOf(storeDocument.getId()))
				.withDocument(operations.getElasticsearchConverter().mapObject(storeDocument))
				.withDocAsUpsert(true)
				.build()).toList();

		operations.bulkUpdate(updateQueries, operations.getIndexCoordinatesFor(StoreDocument.class));
	}

	public Slice<StoreDocument> searchStores(double x, double y, String keyword, Pageable pageable) {
		NativeSearchQuery searchQuery = buildStoreSearchQuery(x, y, keyword, pageable);
		SearchHits<StoreDocument> results = operations.search(searchQuery, StoreDocument.class);
		List<StoreDocument> storeDocuments = results.stream().map(SearchHit::getContent).toList();
		for (StoreDocument storeDocument : storeDocuments) {
			System.out.println("storeDocument = " + storeDocument.toString());
		}
		return toSlice(storeDocuments, pageable);
	}

	public NativeSearchQuery buildStoreSearchQuery(double x, double y, String keyword, Pageable pageable) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		// 키워드 검색 (업체명 또는 아이템명)
		MultiMatchQueryBuilder keywordQuery = QueryBuilders.multiMatchQuery(keyword, "name", "items.name")
			.operator(Operator.OR)
			.type(MultiMatchQueryBuilder.Type.BEST_FIELDS);

		// 거리 필터링
		QueryBuilder geoQuery = QueryBuilders.geoDistanceQuery("location")
			.point(y, x)
			.distance("3km");

		// 업체 상태 필터링
		QueryBuilder statusQuery = QueryBuilders.matchQuery("storeStatus", "OPENED");

		// 모든 조건을 must로 추가
		boolQuery.must(keywordQuery);
		boolQuery.must(geoQuery);
		boolQuery.must(statusQuery);

		// NativeSearchQueryBuilder를 사용하여 쿼리 빌드
		return new NativeSearchQueryBuilder()
			.withQuery(boolQuery)
			.withPageable(pageable)
			.build();
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
