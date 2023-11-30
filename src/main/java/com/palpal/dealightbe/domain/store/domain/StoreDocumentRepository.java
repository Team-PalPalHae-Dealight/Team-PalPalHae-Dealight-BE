package com.palpal.dealightbe.domain.store.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreDocumentRepository extends ElasticsearchRepository<StoreDocument, Long> {
}
