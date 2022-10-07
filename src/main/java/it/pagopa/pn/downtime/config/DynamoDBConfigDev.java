package it.pagopa.pn.downtime.config;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

@Configuration
@EnableDynamoDBRepositories(basePackages = "it.pagopa.pn.downtime.repository")
@Profile("dev")
public class DynamoDBConfigDev {

	@Value("${amazon.dynamodb.log.endpoint}")
	private String amazonDynamoDBEndpointLog;
	
	@Value("${amazon.dynamodb.event.endpoint}")
	private String amazonDynamoDBEndpointEvent;

	@Value("${amazon.dynamodb.accesskey}")
	private String amazonAWSAccessKey;

	@Value("${amazon.dynamodb.secretkey}")
	private String amazonAWSSecretKey;
	
	@Bean(name = "log")
	public AmazonDynamoDB amazonDynamoDBLog() {
		return AmazonDynamoDBClientBuilder.standard().withCredentials(awsCredentialsProvider())
				.withEndpointConfiguration(new EndpointConfiguration(amazonDynamoDBEndpointLog, "us-east-1")).build();
	}
	
	@Bean(name = "event")
	public AmazonDynamoDB amazonDynamoDBEvent() {
		return AmazonDynamoDBClientBuilder.standard().withCredentials(awsCredentialsProvider())
				.withEndpointConfiguration(new EndpointConfiguration(amazonDynamoDBEndpointEvent, "us-east-1")).build();
	}

	private AWSCredentialsProvider awsCredentialsProvider() {
		return new AWSStaticCredentialsProvider(new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey));
	}
		

}
