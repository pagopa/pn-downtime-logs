package it.pagopa.pn.downtime.consumer;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.TemplateException;
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.mapper.CloudwatchMapper;
import it.pagopa.pn.downtime.model.Alarm;
import it.pagopa.pn.downtime.model.MessageCloudwatch;
import it.pagopa.pn.downtime.service.EventService;
import lombok.CustomLog;


@Component
@CustomLog
public class CloudwatchReceiver {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private EventService eventService;

	@Autowired
	private CloudwatchMapper cloudwatchMapper;

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
	 * @throws JSONException
	 */
	@SqsListener(value = "${amazon.sqs.end-point.cloudwatch}", deletionPolicy = SqsMessageDeletionPolicy.ALWAYS)
	public void receiveMessage(final String message) throws InterruptedException, ExecutionException,
			NoSuchAlgorithmException, IOException, TemplateException {
		log.info("threadId : {}, currentTime : {}", Thread.currentThread().getId(), System.currentTimeMillis());
		log.info("message received in CloudWatch queue {}", message);

		MessageCloudwatch messageCloudwatch = mapper.readValue(message, MessageCloudwatch.class);
		Alarm alarm = mapper.readValue(messageCloudwatch.getMessage(), Alarm.class);

		if (Objects.nonNull(messageCloudwatch.getMessage())) {
			PnStatusUpdateEvent pnStatusUpdateEvent = cloudwatchMapper.alarmToPnStatusUpdateEvent(alarm);
			if (!pnStatusUpdateEvent.getFunctionality().isEmpty()) {
				List<PnStatusUpdateEvent> listEvent = new ArrayList<>();
				listEvent.add(pnStatusUpdateEvent);
				eventService.addStatusChangeEvent("PAGO-PA-EVENT_provv", listEvent);
			}
		}
	}
}
