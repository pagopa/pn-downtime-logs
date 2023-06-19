package it.pagopa.pn.downtime.consumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.downtime.generated.openapi.msclient.safestorage.v1.dto.FileCreatedDto;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.service.LegalFactService;
import lombok.CustomLog;

@Component
@CustomLog
public class LegalFactIdReceiver {

	@Autowired
	private DynamoDBMapper dynamoDBMapper;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	LegalFactService legalFactService;

	/**
	 * Receive legal fact.
	 *
	 * @param message the message
	 * @throws JsonProcessingException the json processing exception
	 */
	@SqsListener(value = "${amazon.sqs.end-point.legalfact-available}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
	public void receiveLegalFact(final String message) throws JsonProcessingException {
		log.info("threadId : {}, currentTime : {}", Thread.currentThread().getId(), System.currentTimeMillis());
		PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();
		PnAuditLogEvent logEvent = auditLogBuilder.before(PnAuditLogEventType.AUD_NT_DOWNTIME,
				"message received in Legal Facts queue {}", message)
			.build();
		logEvent.log();
		try {
			FileCreatedDto legalFact = mapper.readValue(message, FileCreatedDto.class);

			Map<String, AttributeValue> eav1 = new HashMap<>();
			eav1.put(":legalFact1", new AttributeValue().withS(legalFact.getKey()));
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
					.withFilterExpression("legalFactId =:legalFact1").withExpressionAttributeValues(eav1);

			List<DowntimeLogs> logs = dynamoDBMapper.scanPage(DowntimeLogs.class, scanExpression).getResults();

			updateFileAvailable(logs, legalFact);
			logEvent.generateSuccess().log();
		} catch (ResourceNotFoundException exc) {
			logEvent.generateFailure(exc.getMessage()).log();
			log.error("STACKTRACE: {}", ExceptionUtils.getStackTrace(exc));
		}
	}
	
	/**
	 * Update file available.
	 *
	 * @param logs the logs
	 * @param legalFact the legal fact
	 */
	private void updateFileAvailable(List<DowntimeLogs> logs, FileCreatedDto legalFact) {
		if (logs != null && !logs.isEmpty()) {
			logs.get(0).setFileAvailable(true);
			log.info("Save legalFactId {}", legalFact.getKey());
			dynamoDBMapper.save(logs.get(0));
		} else {
			throw new ResourceNotFoundException("No Downtime Found for legalFactId {} = " + legalFact.getKey());
		}
	}
}
