package com.palpal.dealightbe.domain.item.infrastructure;


import java.util.List;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Repository;

import com.palpal.dealightbe.domain.item.domain.ItemDocument;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ItemSearchRepositoryImpl {

	private final ElasticsearchOperations operations;

	public void bulkInsertOrUpdate(List<ItemDocument> itemDocuments) {
		List<UpdateQuery> updateQueries = itemDocuments.stream().map(itemDocument ->
			UpdateQuery.builder(String.valueOf(itemDocument.getId()))
				.withDocument(operations.getElasticsearchConverter().mapObject(itemDocument))
				.withDocAsUpsert(true)
				.build()).toList();

		operations.bulkUpdate(updateQueries, operations.getIndexCoordinatesFor(ItemDocument.class));
	}
}
