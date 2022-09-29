package it.pagopa.pn.downtime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

import io.awspring.cloud.messaging.core.QueueMessagingTemplate;

@Configuration
@Profile({"!dev", "!svil"})
public class ConfigSqsProd {
	
	@Value("${amazon.sqs.region.static}")
	private String region;
	@Value("${amazon.sqs.end-point.acts-queue}")
    private String sqsUrlActs;
	@Value("${amazon.sqs.end-point.cloudwatch}")
    private String sqsUrlCloudwatch;
	@Value("${amazon.sqs.end-point.legalfact-available}")
	private String sqsUrlSafeStorage;
	
	@Bean(name = "acts")
    @Primary
    public AmazonSQSAsync amazonSQSAsync1() {
		return AmazonSQSAsyncClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(sqsUrlActs, region))
				.build();
	}
	
	@Bean
	public QueueMessagingTemplate queueMessagingTemplate1() {
		return new QueueMessagingTemplate(amazonSQSAsync1());
	}
	
	@Bean(name = "cloudwatch")
    public AmazonSQSAsync amazonSQSCloudWatch() {
		return AmazonSQSAsyncClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(sqsUrlCloudwatch, region))
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
				.build();
	}
	
	@Bean
	public QueueMessagingTemplate queueMessagingTemplateSafeStorage() {
		return new QueueMessagingTemplate(amazonSQSAsyncSafeStorage());
	}

}
