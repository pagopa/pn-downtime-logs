package it.pagopa.pn.downtime.config;

import org.apache.commons.lang3.StringUtils;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

@Configuration
@EnableDynamoDBRepositories(basePackages = "it.pagopa.pn.downtime.repository")
public class DynamoDBConfig {
	
	public DynamoDBConfig(AwsConfig props) {
		this.props = props;
	}

	private final AwsConfig props;

	@Value("${amazon.dynamodb.log.endpoint}")
	private String amazonDynamoDBEndpointLog;
	
	@Value("${amazon.dynamodb.event.endpoint}")
	private String amazonDynamoDBEndpointEvent;
	
	@Value("${amazon.dynamodb.accesskey}")
	private String amazonAWSAccessKey;

	@Value("${amazon.dynamodb.secretkey}")
	private String amazonAWSSecretKey;

	
	@Bean
	public AmazonDynamoDB amazonDynamoDBLog() {
		if (StringUtils.isNotBlank(props.getEndpointUrl()) && StringUtils.isNotBlank(props.getRegionCode())) {
			return AmazonDynamoDBClientBuilder.standard()
					.withEndpointConfiguration(new EndpointConfiguration(props.getEndpointUrl(), props.getRegionCode()))
					.build();
		} else if (StringUtils.isNotBlank(amazonAWSAccessKey) && StringUtils.isNotBlank(amazonAWSSecretKey)) {
			return AmazonDynamoDBClientBuilder.standard()
					.withCredentials(awsCredentialsProvider())
					.withEndpointConfiguration(new EndpointConfiguration(props.getEndpointUrl(), props.getRegionCode()))
					.build();
		} else {
			return AmazonDynamoDBClientBuilder.standard().build();
		}
	}
	
	private AWSCredentialsProvider awsCredentialsProvider() {
		return new AWSStaticCredentialsProvider(new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey));
	}
	

}
