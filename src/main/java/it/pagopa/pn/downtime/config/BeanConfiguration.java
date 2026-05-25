package it.pagopa.pn.downtime.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.model.Event;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Configuration
@Import(SharedAutoConfiguration.class)
public class BeanConfiguration {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
	   return new RestTemplate();
	}

	@Bean
	public DynamoDbTable<DowntimeLogs> downtimeLogsTable(DynamoDbEnhancedClient enhancedClient) {
		return enhancedClient.table("Downtime-DowntimeLogs", TableSchema.fromBean(DowntimeLogs.class));
	}

	@Bean
	public DynamoDbTable<Event> eventTable(DynamoDbEnhancedClient enhancedClient) {
		return enhancedClient.table("Downtime-Event", TableSchema.fromBean(Event.class));
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
}
