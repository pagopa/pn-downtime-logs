package it.pagopa.pn.downtime.consumer;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.TemplateException;
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import it.pagopa.pn.downtime.mapper.CloudwatchMapper;
import it.pagopa.pn.downtime.model.Alarm;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.service.EventService;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class CloudwatchReceiver {
	@Autowired
	ObjectMapper mapper;
	
	@Autowired
	EventService eventService;
	
	@Autowired
	CloudwatchMapper cloudwatchMapper;
	
	@SqsListener(value = "${amazon.sqs.end-point.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
	public void receiveMessage(final String message) throws InterruptedException, ExecutionException, NoSuchAlgorithmException, IOException, TemplateException {
		Alarm alarm = mapper.readValue(message, Alarm.class);
		PnStatusUpdateEvent pnStatusUpdateEvent = cloudwatchMapper.alarmToPnStatusUpdateEvent(alarm);
		List<PnStatusUpdateEvent> listEvent = new ArrayList<>();
		listEvent.add(pnStatusUpdateEvent);
		log.info("threadId : {}, currentTime : {}", Thread.currentThread().getId(), System.currentTimeMillis());
		log.info("message received {}", pnStatusUpdateEvent.toString());
		eventService.addStatusChangeEvent("PAGO-PA-EVENT_provv", listEvent);
		
		}
}
