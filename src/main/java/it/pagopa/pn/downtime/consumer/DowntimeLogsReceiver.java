package it.pagopa.pn.downtime.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.service.LegalFactService;
import lombok.extern.slf4j.Slf4j;


/** The Constant log. */
@Slf4j
@Component
public class DowntimeLogsReceiver {

	/** The mapper. */
	@Autowired
	ObjectMapper mapper;
	
	/** The legal fact service. */
	@Autowired
	LegalFactService legalFactService;
	
	/** The dynamo DB mapper. Log */
	@Autowired
	@Qualifier("logMapper")
	private DynamoDBMapper dynamoDBMapperLog;
	
	/**
	 * Receive string message from a sqs queue which will be used for the legal fact generation .
	 *
	 * @param message the message that contains the downtime log
	 * @throws Exception 
	 */
	@SqsListener(value = "${amazon.sqs.end-point.acts-queue}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
	public void receiveStringMessage(final String message) throws Exception {
		DowntimeLogs downtimeLog = mapper.readValue(message, DowntimeLogs.class);
		log.info("threadId : {}, currentTime : {}", Thread.currentThread().getId(), System.currentTimeMillis());
		log.info("message received in Acts queue {}", downtimeLog.toString());
		downtimeLog = legalFactService.generateLegalFact(downtimeLog);
		dynamoDBMapperLog.save(downtimeLog);
		}
	}
