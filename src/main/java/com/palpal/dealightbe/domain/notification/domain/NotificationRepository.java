package com.palpal.dealightbe.domain.notification.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	
	Slice<Notification> findAllByMemberIdAndIsReadFalse(Long memberId, Pageable pageable);

	Slice<Notification> findAllByStoreIdAndIsReadFalse(Long storeId, Pageable pageable);

	@Modifying
	@Query("DELETE FROM Notification n WHERE n.isRead = true")
	void deleteReadNotifications();
}
