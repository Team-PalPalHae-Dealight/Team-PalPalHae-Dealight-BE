package com.palpal.dealightbe.domain.notification.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findAllByMemberIdAndIsReadFalse(Long memberId);

	@Modifying
	@Query("DELETE FROM Notification n WHERE n.isRead = true")
	void deleteReadNotifications();
}
