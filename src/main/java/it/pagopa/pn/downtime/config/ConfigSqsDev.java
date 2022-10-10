package it.pagopa.pn.downtime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import lombok.RequiredArgsConstructor;

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
    private String sqsUrlActs;
	@Value("${amazon.sqs.end-point.cloudwatch}")
    private String sqsUrlCloudwatch;
	@Value("${amazon.sqs.end-point.legalfact-available}")
	private String sqsUrlSafeStorage;
	
	@Bean(name = "acts")
    @Primary
    public AmazonSQSAsync amazonSQSAsync() {
		return AmazonSQSAsyncClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(sqsUrlActs, region))
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
				.build();
	}
	
	@Bean
	public QueueMessagingTemplate queueMessagingTemplate() {
		return new QueueMessagingTemplate(amazonSQSAsync());
	}
	
	@Bean(name = "cloudwatch")
    public AmazonSQSAsync amazonSQSCloudWatch() {
		return AmazonSQSAsyncClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(sqsUrlCloudwatch, region))
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
				.build();
	}
	
	@Bean
	public QueueMessagingTemplate queueMessagingTemplateCloudWatch() {
		return new QueueMessagingTemplate(amazonSQSCloudWatch());
	}
	
	@Bean(name = "safestorage")
    public AmazonSQSAsync amazonSQSAsyncSafeStorage() {
		return AmazonSQSAsyncClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(sqsUrlSafeStorage, region))
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
				.build();
	}
	
	@Bean
	public QueueMessagingTemplate queueMessagingTemplateSafeStorage() {
		return new QueueMessagingTemplate(amazonSQSAsyncSafeStorage());
	}
}
