package it.pagopa.pn.downtime.config;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

@Configuration
@EnableDynamoDBRepositories(basePackages = "it.pagopa.pn.downtime.repository")
@Profile({"!dev", "!svil"})
public class DynamoDBConfigProd {

	@Value("${amazon.dynamodb.log.endpoint}")
	private String amazonDynamoDBEndpointLog;
	
	@Value("${amazon.dynamodb.event.endpoint}")
	private String amazonDynamoDBEndpointEvent;


	
	@Bean(name = "log")
	@Primary
	public AmazonDynamoDB amazonDynamoDBLog() {
		System.out.println("test11");
		return AmazonDynamoDBClientBuilder.standard()
				.withEndpointConfiguration(new EndpointConfiguration(amazonDynamoDBEndpointLog, "us-east-1"))
				.build();
	}
	
	@Bean(name = "event")
	public AmazonDynamoDB amazonDynamoDBEvent() {
		System.out.println("test11");
		return AmazonDynamoDBClientBuilder.standard()
				.withEndpointConfiguration(new EndpointConfiguration(amazonDynamoDBEndpointEvent, "us-east-1"))
				.build();
	}

}
