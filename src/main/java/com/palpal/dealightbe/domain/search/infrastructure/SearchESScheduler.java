package com.palpal.dealightbe.domain.search.infrastructure;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.palpal.dealightbe.domain.search.application.SearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchESScheduler {

	private final SearchService searchService;

	// 업로드 메서드를 1시간마다 실행
	@Scheduled(cron = "0 0 0/1 * * *")
	public void uploadToES() {
		searchService.uploadToES();
	}

	// 업데이트 메서드를 30분마다 실행
	@Scheduled(cron = "0 0/30 * * * *")
	public void updateStatusToES() {
		searchService.updateStatusToES();
	}
}
