package it.pagopa.pn.downtime.consumer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import it.pagopa.pn.downtime.dto.response.GetLegalFactDto;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.service.LegalFactService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LegalFactIdReceiver {
	
	/** The dynamo DB mapper. Log */
	@Autowired
	private DynamoDBMapper dynamoDBMapper;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	LegalFactService legalFactService;

	@SqsListener(value = "${amazon.sqs.end-point.legalfact-available}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
	public void receiveLegalFact(final String message) throws IOException {
		log.info("threadId : {}, currentTime : {}", Thread.currentThread().getId(), System.currentTimeMillis());
		log.info("message received in Legal Facts queue {}", message);	
		GetLegalFactDto legalFact = mapper.readValue(message, GetLegalFactDto.class);
		
		Map<String, AttributeValue> eav1 = new HashMap<>();
		eav1.put(":legalFact1", new AttributeValue().withS(legalFact.getKey()));
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
			    .withFilterExpression("legalFactId =:legalFact1")
			    .withExpressionAttributeValues(eav1).withLimit(1);
		
		List<DowntimeLogs> logs = dynamoDBMapper.scanPage(DowntimeLogs.class, scanExpression).getResults();
	
		if (logs != null && !logs.isEmpty()) {
			logs.get(0).setFileAvailable(true);
			dynamoDBMapper.save(logs.get(0));
		}
	  }
	}
