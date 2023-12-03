package com.palpal.dealightbe.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class BucketConfig {

	private static final int CAPACITY = 30;
	private static final int DURATION = 5;
	private static final int REFILL_TOKENS_COUNT = 5;

	@Bean
	public Bucket bucket() {
		final Refill refill = Refill.intervally(REFILL_TOKENS_COUNT, Duration.ofSeconds(DURATION));

		final Bandwidth limit = Bandwidth.classic(CAPACITY, refill);

		return Bucket.builder()
			.addLimit(limit)
			.build();
	}
}
