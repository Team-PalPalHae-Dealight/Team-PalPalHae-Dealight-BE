package com.palpal.dealightbe.global.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.palpal.dealightbe.domain.notification.domain.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@StepScope
@Slf4j
public class NotificationTasklet implements Tasklet {

	private final NotificationRepository notificationRepository;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

		log.info(contribution.toString());
		log.info(chunkContext.toString());
		log.info(">>> Delete ReadNotifications >>>");

		notificationRepository.deleteReadNotifications();

		return RepeatStatus.FINISHED;
	}
}
