package com.palpal.dealightbe.domain.notification.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findAllByMemberIdAndIsReadFalse(Long memberId);

}
