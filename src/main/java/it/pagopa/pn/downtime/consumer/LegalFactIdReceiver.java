package it.pagopa.pn.downtime.consumer;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import it.pagopa.pn.downtime.dto.response.GetLegalFactDto;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
import it.pagopa.pn.downtime.service.LegalFactService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LegalFactIdReceiver {
	
	@Autowired
	DowntimeLogsRepository downtimeLogsRepository;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	LegalFactService legalFactService;

	@SqsListener(value = "${amazon.sqs.end-point.legalfact-available}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
	public void receiveLegalFact(final String message) throws IOException {
		log.info("threadId : {}, currentTime : {}", Thread.currentThread().getId(), System.currentTimeMillis());
		log.info("message received in Legal Facts queue {}", message);	
		GetLegalFactDto legalFact = mapper.readValue(message, GetLegalFactDto.class);
		
		DowntimeLogs downtimeLogFiltered = downtimeLogsRepository.findFirstByLegalFactId(legalFact.getKey());
		
		if (downtimeLogFiltered != null) {
			downtimeLogFiltered.setFileAvailable(true);
			downtimeLogsRepository.save(downtimeLogFiltered);
		}
	  }
	}
