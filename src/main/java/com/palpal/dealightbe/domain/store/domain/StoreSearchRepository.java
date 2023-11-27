package com.palpal.dealightbe.domain.store.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreSearchRepository extends ElasticsearchRepository<StoreDocument, Long> {
}
