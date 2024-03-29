package it.pagopa.pn.downtime.consumer;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.TemplateException;
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.service.LegalFactService;
import lombok.CustomLog;



@Component
@CustomLog
public class DowntimeLogsReceiver {

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private LegalFactService legalFactService;
	
	@Autowired
	private DynamoDBMapper dynamoDBMapper;
	
	/**
	 * Receive string message from a sqs queue which will be used for the legal fact generation .
	 *
	 * @param message the message that contains the downtime log
	 * @throws TemplateException 
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 * @throws Exception 
	 */
	@SqsListener(value = "${amazon.sqs.end-point.acts-queue}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
	public void receiveStringMessage(final String message) throws NoSuchAlgorithmException, IOException, TemplateException {
		DowntimeLogs downtimeLog = mapper.readValue(message, DowntimeLogs.class);
		log.info("threadId : {}, currentTime : {}", Thread.currentThread().getId(), System.currentTimeMillis());
		log.info("message received in Acts queue {}", downtimeLog.toString());
		downtimeLog = legalFactService.generateLegalFact(downtimeLog);
		dynamoDBMapper.save(downtimeLog);
		}
	}
