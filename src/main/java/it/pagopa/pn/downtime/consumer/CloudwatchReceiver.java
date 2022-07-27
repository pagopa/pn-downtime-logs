package it.pagopa.pn.downtime.consumer;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.TemplateException;
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import it.pagopa.pn.downtime.mapper.CloudwatchMapper;
import it.pagopa.pn.downtime.model.Alarm;
import it.pagopa.pn.downtime.model.MessageCloudwatch;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.service.EventService;
import lombok.extern.slf4j.Slf4j;

/** The Constant log. */
@Slf4j
@Component
public class CloudwatchReceiver {

	/** The mapper. */
	@Autowired
	ObjectMapper mapper;

	/** The event service. */
	@Autowired
	EventService eventService;

	/** The cloudwatch mapper. */
	@Autowired
	CloudwatchMapper cloudwatchMapper;

	/**
	 * Receive message from the cloudwatch sqs queue which will be used to register
	 * a new event.
	 *
	 * @param message the message which contains the new event
	 * @throws InterruptedException     the interrupted exception
	 * @throws ExecutionException       the execution exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws IOException              Signals that an I/O exception has occurred.
	 * @throws TemplateException        the template exception
	 */
	@SqsListener(value = "${amazon.sqs.end-point.cloudwatch}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
	public void receiveMessage(final String message)
			throws InterruptedException, ExecutionException, NoSuchAlgorithmException, IOException, TemplateException {
		MessageCloudwatch messageCloudwatch = mapper.readValue(message, MessageCloudwatch.class);
		if (Objects.nonNull(messageCloudwatch) && Objects.nonNull(messageCloudwatch.getAlarm())) {
			Alarm alarm = messageCloudwatch.getAlarm();
			PnStatusUpdateEvent pnStatusUpdateEvent = cloudwatchMapper.alarmToPnStatusUpdateEvent(alarm);
			List<PnStatusUpdateEvent> listEvent = new ArrayList<>();
			listEvent.add(pnStatusUpdateEvent);
			log.info("threadId : {}, currentTime : {}", Thread.currentThread().getId(), System.currentTimeMillis());
			log.info("message received {}", pnStatusUpdateEvent.toString());
			eventService.addStatusChangeEvent("PAGO-PA-EVENT_provv", listEvent);
		}
	}
}
