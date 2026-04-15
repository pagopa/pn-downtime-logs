package it.pagopa.pn.downtime.config;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

@Configuration
public class DynamoDBConfig {

	public DynamoDBConfig(AwsConfigsActivation props) {
		this.props = props;
	}

	private final AwsConfigsActivation props;

	@Value("${amazon.dynamodb.accesskey}")
	private String amazonAWSAccessKey;

	@Value("${amazon.dynamodb.secretkey}")
	private String amazonAWSSecretKey;

	@Bean
	public DynamoDbClient dynamoDbClient() {
		DynamoDbClientBuilder builder = DynamoDbClient.builder();
		if (StringUtils.isNotBlank(props.getEndpointUrl()) && StringUtils.isNotBlank(props.getRegionCode())) {
			builder.endpointOverride(URI.create(props.getEndpointUrl()))
				   .region(Region.of(props.getRegionCode()));
		} else if (StringUtils.isNotBlank(amazonAWSAccessKey) && StringUtils.isNotBlank(amazonAWSSecretKey)) {
			builder.credentialsProvider(StaticCredentialsProvider.create(
					AwsBasicCredentials.create(amazonAWSAccessKey, amazonAWSSecretKey)));
		}
		return builder.build();
	}
}
