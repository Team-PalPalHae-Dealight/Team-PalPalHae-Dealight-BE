package com.palpal.dealightbe.domain.item.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemSearchRepository extends ElasticsearchRepository<ItemDocument, Long> {
}
