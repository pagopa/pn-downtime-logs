package it.pagopa.pn.downtime.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("aws")
@Getter
@Setter
@Configuration
public class AwsConfig {
	private String regionCode;
	private String endpointUrl;
}
