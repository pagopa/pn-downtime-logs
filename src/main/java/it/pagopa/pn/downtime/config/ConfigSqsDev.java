package it.pagopa.pn.downtime.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
@RequiredArgsConstructor
@Profile("dev")
public class ConfigSqsDev {

	@Value("${amazon.sqs.region.static}")
	private String region;
	@Value("${amazon.sqs.credentials.accessKey}")
	private String accessKey;
	@Value("${amazon.sqs.credentials.secretKey}")
	private String secretKey;
	@Value("${amazon.sqs.end-point.acts-queue}")
	private String sqsEndpoint;

	@Bean
	@Primary
	public SqsAsyncClient sqsAsyncClient() {
		return SqsAsyncClient.builder()
				.endpointOverride(URI.create(sqsEndpoint))
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(accessKey, secretKey)))
				.build();
	}
}
