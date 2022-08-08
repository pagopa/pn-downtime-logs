package it.pagopa.pn.downtime.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.pagopa.pn.downtime.mapper.DowntimeLogsMapper;
import it.pagopa.pn.downtime.model.DowntimeLogs;


/**
 * The Class DowntimeLogsSend.
 */
@Service
public class DowntimeLogsSend {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DowntimeLogsSend.class);

    /** The amazon SQS. */
    @Autowired
    @Qualifier("acts")
    private AmazonSQS amazonSQS;

    /** The object mapper. */
    @Autowired
    private ObjectMapper objectMapper;
	
	/** The mapper downtime logs mapper. */
	@Autowired
	DowntimeLogsMapper mapperDowntimeLogsMapper;

	/**
	 * Send a new message to the sqs queue which will be used for the legal fact generation.
	 *
	 * @param downtimeLogs the downtime logs which will be used for the creation of the message
	 * @param url the url of the sqs queue
	 * @throws JsonProcessingException the json processing exception
	 */
	public void sendMessage(DowntimeLogs downtimeLogs, String url) throws JsonProcessingException {
		SendMessageRequest sendMessageRequest = null;
            sendMessageRequest = new SendMessageRequest().withQueueUrl(url)
                    .withMessageBody(objectMapper.writeValueAsString(downtimeLogs));
            amazonSQS.sendMessage(sendMessageRequest);
            LOGGER.info("Event has been published in SQS.");
	}
}
