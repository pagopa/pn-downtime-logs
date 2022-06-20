package it.pagopa.pn.downtime.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

import io.awspring.cloud.messaging.core.QueueMessagingTemplate;

@Configuration
public class ConfigSqs {
	

	@Bean
	public QueueMessagingTemplate queueMessagingTemplateCustom() {
		return new QueueMessagingTemplate(amazonSQSAsync());
	}

	private AmazonSQSAsync amazonSQSAsync() {
		return AmazonSQSAsyncClientBuilder.standard().withRegion(Regions.SA_EAST_1)
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("admin", "admin"))).build();
	}


}
