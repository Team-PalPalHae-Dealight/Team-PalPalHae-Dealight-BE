package com.palpal.dealightbe.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.palpal.dealightbe.global.batch.tasklet.NotificationTasklet;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class BatchConfig {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final NotificationTasklet notificationTasklet;

	@Bean
	public Job notificationJob() {
		return jobBuilderFactory.get("notificationJob")
			.start(notificationStep())
			.build();
	}

	@Bean
	@JobScope
	public Step notificationStep() {
		return stepBuilderFactory.get("notificationStep")
			.tasklet(notificationTasklet)
			.build();
	}
}
