package com.palpal.dealightbe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "els")
@Getter
@Setter
public class ElasticSearchProperty {

	private String host;
	private int port;
}
