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
import org.apache.commons.lang3.StringUtils;

@Configuration
@EnableDynamoDBRepositories(basePackages = "it.pagopa.pn.downtime.repository")
@Profile({"!dev & !svil"})
public class DynamoDBConfigProd {
	
	public DynamoDBConfigProd(AwsConfig props) {
		this.props = props;
	}

	private final AwsConfig props;

	@Value("${amazon.dynamodb.log.endpoint}")
	private String amazonDynamoDBEndpointLog;
	
	@Value("${amazon.dynamodb.event.endpoint}")
	private String amazonDynamoDBEndpointEvent;


	
	@Bean(name = "log")
	@Primary
	public AmazonDynamoDB amazonDynamoDBLog() {
		if (StringUtils.isNotBlank(props.getEndpointUrl()) && StringUtils.isNotBlank(props.getRegionCode())){
			return AmazonDynamoDBClientBuilder.standard()
					.withEndpointConfiguration(new EndpointConfiguration(props.getEndpointUrl(), props.getRegionCode()))
					.build();
		} else {	
			return AmazonDynamoDBClientBuilder.standard()
					.build();
		}
	}
	
	@Bean(name = "event")
	public AmazonDynamoDB amazonDynamoDBEvent() {
		if (StringUtils.isNotBlank(props.getEndpointUrl()) && StringUtils.isNotBlank(props.getRegionCode())){
			return AmazonDynamoDBClientBuilder.standard()
					.withEndpointConfiguration(new EndpointConfiguration(props.getEndpointUrl(), props.getRegionCode()))
					.build();
		} else {	
			return AmazonDynamoDBClientBuilder.standard()
					.build();
		}
	}

}
