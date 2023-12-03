package com.palpal.dealightbe.domain.store.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UpdatedStoreRepository extends JpaRepository<UpdatedStore, Long> {

	@Query("SELECT us FROM UpdatedStore us WHERE us.documentStatus != 'DONE'")
	List<UpdatedStore> findAllByDocumentStatusIsReady();

	@Query("SELECT us FROM UpdatedStore us WHERE us.documentStatus != 'READY'")
	List<UpdatedStore> findAllByDocumentStatusIsDone();
}
