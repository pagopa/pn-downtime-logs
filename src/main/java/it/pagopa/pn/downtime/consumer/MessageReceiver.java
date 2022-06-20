package it.pagopa.pn.downtime.consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import it.pagopa.pn.downtime.model.Alarm;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MessageReceiver {

	@Autowired
	ObjectMapper mapper;
	
	@SqsListener(value = "${cloud.aws.fila.test}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS )
	public void receiveStringMessage(final String message) throws JsonMappingException, JsonProcessingException {
		
		Alarm alarm = mapper.readValue(message, Alarm.class);
		
		log.info("message received {}",alarm.toString());
	}

}