package it.pagopa.pn.downtime.consumer;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.TemplateException;
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
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
	
	/** The downtime logs repository. */
	@Autowired
	DowntimeLogsRepository downtimeLogsRepository;
	
	/**
	 * Receive string message from a sqs queue which will be used for the legal fact generation .
	 *
	 * @param message the message that contains the downtime log
	 * @throws Exception 
	 */
	@SqsListener(value = "${amazon.sqs.end-point.attidagenerare}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
	public void receiveStringMessage(final String message) throws Exception {
		DowntimeLogs downtimeLog = mapper.readValue(message, DowntimeLogs.class);
		log.info("threadId : {}, currentTime : {}", Thread.currentThread().getId(), System.currentTimeMillis());
		log.info("message received {}", downtimeLog.toString());
		downtimeLog = legalFactService.generateLegalFact(downtimeLog);
		downtimeLogsRepository.save(downtimeLog);
		}
	}
