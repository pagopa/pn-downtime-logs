package it.pagopa.pn.downtime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
@Profile({"!dev", "!svil"})
public class ConfigSqsProd {

	@Value("${amazon.sqs.region.static}")
	private String region;

	@Bean
	@Primary
	public SqsAsyncClient sqsAsyncClient() {
		return SqsAsyncClient.builder()
				.region(Region.of(region))
				.build();
	}
}
