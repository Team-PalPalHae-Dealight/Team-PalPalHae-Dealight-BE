package com.palpal.dealightbe.domain.item.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UpdatedItemRepository extends JpaRepository<UpdatedItem, Long> {

	@Query("SELECT ui FROM UpdatedItem ui WHERE ui.documentStatus != 'DONE'")
	List<UpdatedItem> findAllByDocumentStatusIsReady();
}
