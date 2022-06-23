package it.pagopa.pn.downtime.consumer;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import it.pagopa.pn.downtime.dto.request.MessageStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MessageReceiver {

	@Autowired
	ObjectMapper mapper;
	
	@SqsListener(value = "${cloud.aws.end-point.uri}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
	public void receiveStringMessage(final String message) throws JsonProcessingException, InterruptedException, ExecutionException {
		MessageStatus messageStatus = mapper.readValue(message, MessageStatus.class);
		log.info("threadId : {}, currentTime : {}", Thread.currentThread().getId(), System.currentTimeMillis());
		log.info("message received {}", messageStatus.toString());
		}
	}
