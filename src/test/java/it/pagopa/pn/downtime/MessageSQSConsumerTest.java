package it.pagopa.pn.downtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.pagopa.pn.downtime.consumer.CloudwatchReceiver;
import it.pagopa.pn.downtime.consumer.DowntimeLogsReceiver;
import it.pagopa.pn.downtime.consumer.LegalFactIdReceiver;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.service.LegalFactService;

@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)

public class MessageSQSConsumerTest extends AbstractMock {

	@Autowired
	CloudwatchReceiver mockCloudwatchReceiver;

	@Autowired
	LegalFactIdReceiver mockLegalFactIdReceiver;
	
	@Autowired
	DowntimeLogsReceiver mockDowntimeLogsReceiver;
	
	@Autowired
	LegalFactService legalFactService;

	@Autowired
	ObjectMapper mapper;

	@Test
	public void test_messageMockCloudwatchReceiver() throws Throwable {
		String messageCloudwatch = getMessageCloudwatchFromResource();

		mockCloudwatchReceiver.receiveMessage(messageCloudwatch);
		mockAddStatusChange_KO(client);

		Assertions.assertTrue(true);
	}

	@Test
	public void test_messageMockLegalFactIdReceiver() throws Throwable {

		DowntimeLogs dt = getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"),
				PnFunctionality.NOTIFICATION_CREATE, "EVENT", "akdocdfe-50403",
				OffsetDateTime.parse("2022-08-28T09:55:15.995Z"));
		dt.setFileAvailable(true);
		dt.setLegalFactId("PN_DOWNTIME_LEGAL_FACTS-TEST");

		mockFindFirstByLegalFactId(dt);
		mockSaveDowntime();

		String messageLegalFactId = getMessageLegalFactIdFromResource();

		mockLegalFactIdReceiver.receiveLegalFact(messageLegalFactId);

        Assertions.assertNotNull(dt.getFileAvailableTimestamp());
	}

	@Test
	public void test_messageMockActsQueueReceiver() throws Throwable {
		mockAddStatusChange_OK(client);
		String messageActsQueue = getMessageActsQueueFromResource();
		mockDowntimeLogsReceiver.receiveStringMessage(messageActsQueue);

		DowntimeLogs dt = mapper.readValue(messageActsQueue, DowntimeLogs.class);
		mockSaveDowntime();

		assertThat(legalFactService.generateLegalFact(dt).toString()).contains("legalFactId");
	}
}
