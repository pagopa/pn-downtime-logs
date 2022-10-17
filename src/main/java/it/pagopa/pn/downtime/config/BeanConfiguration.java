package it.pagopa.pn.downtime.config;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

@Configuration
public class BeanConfiguration {
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
	   return new RestTemplate();
	}
	
	@Bean
	public RestTemplate simpleRestTemplate() {
		//TODO: START -  to delete when deploying in dev environment, this is just for local test purposes
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

			@Override
		    public boolean verify(String hostname, SSLSession session) {
		        return true;
		    }
		});
		//END
		return new RestTemplate();
	}

	@Bean(name = "logMapper")
	@Primary
	public DynamoDBMapper amazonDBMapperLogs(@Autowired @Qualifier("log") AmazonDynamoDB amazonDynamoDB) {
		return new DynamoDBMapper(amazonDynamoDB);
	}

	@Bean(name = "eventMapper")
	public DynamoDBMapper amazonDBMapperEvent(@Autowired @Qualifier("event") AmazonDynamoDB amazonDynamoDB) {
		return new DynamoDBMapper(amazonDynamoDB);
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
