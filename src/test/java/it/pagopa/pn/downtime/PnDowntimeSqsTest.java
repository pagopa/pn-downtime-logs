package it.pagopa.pn.downtime;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.producer.DowntimeLogsSend;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PnDowntimeSqsTest extends AbstractMock {

	@InjectMocks
	DowntimeLogsSend downtimeLogsSend;

	@Mock
	private SqsAsyncClient sqsClient;

	@Mock
	ObjectMapper objectMapper;

	@Test
	void sendMessageToSQS() throws JsonProcessingException {
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("{}");
		Mockito.when(sqsClient.sendMessage(Mockito.any(SendMessageRequest.class)))
				.thenReturn(CompletableFuture.completedFuture(SendMessageResponse.builder().build()));

		DowntimeLogs dt = getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"),
				PnFunctionality.NOTIFICATION_CREATE, "EVENT", "akdocdfe-50403",
				OffsetDateTime.parse("2022-08-28T09:55:15.995Z"));
		downtimeLogsSend.sendMessage(dt, "testurl");
		Assertions.assertTrue(true);
	}
}
