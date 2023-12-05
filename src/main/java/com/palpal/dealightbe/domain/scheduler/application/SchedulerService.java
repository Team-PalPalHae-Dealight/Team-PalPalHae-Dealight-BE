package com.palpal.dealightbe.domain.scheduler.application;

import static com.palpal.dealightbe.domain.order.domain.Order.STORAGE_PERIOD_MONTH;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.item.domain.ItemRepository;
import com.palpal.dealightbe.domain.notification.domain.NotificationRepository;
import com.palpal.dealightbe.domain.order.domain.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SchedulerService {

	private final ItemRepository itemRepository;
	private final OrderRepository orderRepository;
	private final NotificationRepository notificationRepository;

	private static final String NOTIFICATION_DEFAULT_SCHEDULING_PERIOD = "0 0 2 ? * TUE";  //매주 화요일 새벽 2시에 진행
	private static final String ORDER_STORAGE_PERIOD = "* * 1 * * ?";
	private static final String ITEM_STORAGE_PERIOD = "* * 1 * * ?";

	@Scheduled(cron = NOTIFICATION_DEFAULT_SCHEDULING_PERIOD)
	public void cleanupReadNotifications() {
		notificationRepository.deleteReadNotifications();

		log.info("RUN:CLEANUP_READ_NOTIFICATIONS:TIME : {}", LocalDateTime.now());
		log.info("RUN:CLEANUP_READ_NOTIFICATIONS:THREAD : {}", Thread.currentThread().getName());
	}

	@Scheduled(cron = ORDER_STORAGE_PERIOD)
	public void cleanUpOrders() {
		List<Long> orderIdsToDelete = orderRepository.findAllByUpdatedMoreThan(STORAGE_PERIOD_MONTH);

		if (!orderIdsToDelete.isEmpty()) {
			orderRepository.deleteAllInBatchByIdIn(orderIdsToDelete);
		}
	}

	@Scheduled(cron = ITEM_STORAGE_PERIOD)
	public void cleanUpItems() {
		itemRepository.clearItemsDeleted();
	}
}
