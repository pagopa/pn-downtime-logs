package it.pagopa.pn.downtime.config;

import org.apache.commons.lang3.StringUtils;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

@Configuration
@EnableDynamoDBRepositories
(basePackages = "it.pagopa.pn.downtime.repository")
public class DynamoDBConfig {

	@Value("${amazon.dynamodb.endpoint}")
	private String amazonDynamoDBEndpoint;

	@Value("${amazon.dynamodb.accesskey}")
	private String amazonAWSAccessKey;

	@Value("${amazon.dynamodb.secretkey}")
	private String amazonAWSSecretKey;


//	@Bean
//	public AmazonDynamoDB amazonDynamoDB() {
//		AmazonDynamoDB amazonDynamoDB 
//		= new AmazonDynamoDBClient(amazonAWSCredentials());
//
//		if (!StringUtils.isEmpty(amazonDynamoDBEndpoint)) {
//			amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
//		}
//
//		return amazonDynamoDB;
//	}
//
//	@Bean
//	public AWSCredentials amazonAWSCredentials() {
//		return new BasicAWSCredentials(
//				amazonAWSAccessKey, amazonAWSSecretKey);
//	}
	
    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
            .withCredentials(awsCredentialsProvider())
            .withEndpointConfiguration(new EndpointConfiguration(amazonDynamoDBEndpoint, "us-east-1"))
            .build();
    }
    
    private AWSCredentialsProvider awsCredentialsProvider(){
        return new AWSStaticCredentialsProvider(
               new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey));
    }
}
