package it.pagopa.pn.downtime.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

@Configuration
public class BeanConfiguration {
	
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
	   return new RestTemplate();
	}
	
	@Bean
	@Primary
	public DynamoDBMapper amazonDBMapper(@Autowired AmazonDynamoDB amazonDynamoDB) {
		return new DynamoDBMapper(amazonDynamoDB);
	}
	
}
