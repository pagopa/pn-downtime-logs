package it.pagopa.pn.downtime.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.sqs.annotation.SqsListener;
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

	@SqsListener(value = "${amazon.sqs.end-point.cloudwatch}")
	public void receiveMessage(final String message) {
		log.info("threadId : {}, currentTime : {}", Thread.currentThread().getId(), System.currentTimeMillis());
		log.info("message received in CloudWatch queue {}", message);
		try {
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
		} catch (Exception e) {
			log.error("Error processing CloudWatch message: {}", e.getMessage(), e);
		}
	}
}
