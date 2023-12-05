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
	private static final int BATCH_SIZE = 500;

	public void bulkInsertOrUpdate(List<ItemDocument> itemDocuments) {
		for (int i = 0; i < itemDocuments.size(); i += BATCH_SIZE) {
			int endIndex = Math.min(i + BATCH_SIZE, itemDocuments.size());
			List<ItemDocument> batch = itemDocuments.subList(i, endIndex);

			bulkIndexBatch(batch);
		}
	}

	private void bulkIndexBatch(List<ItemDocument> batch) {
		List<UpdateQuery> updateQueries = batch.stream().map(itemDocument ->
			UpdateQuery.builder(String.valueOf(itemDocument.getId()))
				.withDocument(operations.getElasticsearchConverter().mapObject(itemDocument))
				.withDocAsUpsert(true)
				.build()).toList();

		operations.bulkUpdate(updateQueries, operations.getIndexCoordinatesFor(ItemDocument.class));
	}
}
