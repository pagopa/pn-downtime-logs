package it.pagopa.pn.downtime.consumer;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

@Component
@CustomLog
public class LegalFactIdReceiver {

	@Autowired
	private DynamoDbTable<DowntimeLogs> downtimeLogsTable;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	LegalFactService legalFactService;

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

			Expression scanFilter = Expression.builder()
					.expression("legalFactId =:legalFact1")
					.expressionValues(Map.of(":legalFact1", AttributeValue.builder().s(legalFact.getKey()).build()))
					.build();

			ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
					.filterExpression(scanFilter)
					.build();

			List<DowntimeLogs> logs = downtimeLogsTable.scan(scanRequest).items().stream().toList();

			updateFileAvailable(logs, legalFact);
			logEvent.generateSuccess().log();
		} catch (ResourceNotFoundException exc) {
			logEvent.generateFailure(exc.getMessage()).log();
			log.error("STACKTRACE: {}", ExceptionUtils.getStackTrace(exc));
		}
	}

	private void updateFileAvailable(List<DowntimeLogs> logs, FileCreatedDto legalFact) {
		if (logs != null && !logs.isEmpty()) {
			DowntimeLogs downtimeLogs = logs.get(0);
			downtimeLogs.setFileAvailable(true);
			OffsetDateTime fileAvailableTimestamp = OffsetDateTime.now(ZoneOffset.UTC);
			downtimeLogs.setFileAvailableTimestamp(fileAvailableTimestamp);
			log.info("Save legalFactId {} with timestamp {}", legalFact.getKey(), fileAvailableTimestamp);
			downtimeLogsTable.putItem(downtimeLogs);
		} else {
			throw ResourceNotFoundException.builder()
					.message("No Downtime Found for legalFactId {} = " + legalFact.getKey())
					.build();
		}
	}
}
