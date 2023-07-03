package it.pagopa.pn.downtime;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.producer.DowntimeLogsSend;

@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PnDowntimeSqsTest extends AbstractMock {

	@InjectMocks
	DowntimeLogsSend downtimeLogsSend;

	@Mock
	@Qualifier("acts")
	private AmazonSQS amazonSQS;
	
	@Mock
	ObjectMapper mapper;

	@Test
	void sendMessageToSQS() throws JsonProcessingException {
		Mockito.when(amazonSQS.sendMessage(Mockito.any(SendMessageRequest.class))).thenReturn(new SendMessageResult());

		DowntimeLogs dt = getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"),
				PnFunctionality.NOTIFICATION_CREATE, "EVENT", "akdocdfe-50403",
				OffsetDateTime.parse("2022-08-28T09:55:15.995Z"));
		downtimeLogsSend.sendMessage(dt, "testurl");
		Assertions.assertTrue(true);
	}
}
