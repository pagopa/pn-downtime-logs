package it.pagopa.pn.downtime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

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
	
	@Bean
	public ObjectMapper getObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModule(new ParameterNamesModule());
		mapper.registerModule(new Jdk8Module());
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}
	
	@Bean
    protected MessageConverter messageConverter(ObjectMapper objectMapper) {

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setSerializedPayloadClass(String.class);
        converter.setStrictContentTypeMatch(false);
        return converter;
    }

}
