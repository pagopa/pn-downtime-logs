package it.pagopa.pn.downtime.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import it.pagopa.pn.downtime.dto.request.MessageStatus;
import it.pagopa.pn.downtime.mapper.DowntimeLogsMapper;
import it.pagopa.pn.downtime.model.DowntimeLogs;

@Service
public class DowntimeLogsSend {
	private static final Logger LOGGER = LoggerFactory.getLogger(DowntimeLogsSend.class);

    @Autowired
    private AmazonSQS amazonSQS;

    @Autowired
    private ObjectMapper objectMapper;
	@Autowired
	DowntimeLogsMapper mapperDowntimeLogsMapper;

	public void sendMessage(DowntimeLogs downtimeLogs, QueueMessagingTemplate queueMessagingTemplate, String url) throws JsonProcessingException {
		MessageStatus body = mapperDowntimeLogsMapper.downtimeLogsToMessageStatus(downtimeLogs);
		SendMessageRequest sendMessageRequest = null;
		try {
            sendMessageRequest = new SendMessageRequest().withQueueUrl(url)
                    .withMessageBody(objectMapper.writeValueAsString(body));
            amazonSQS.sendMessage(sendMessageRequest);
            LOGGER.info("Event has been published in SQS.");
        } catch (JsonProcessingException e) {
            LOGGER.error("JsonProcessingException e : {} and stacktrace : {}", e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Exception ocurred while pushing event to sqs : {} and stacktrace ; {}", e.getMessage(), e);
        }
		//queueMessagingTemplate.send(url, MessageBuilder.withPayload(jsonText).build());
	}
}