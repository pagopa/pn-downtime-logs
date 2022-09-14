package it.pagopa.pn.downtime;

import java.time.OffsetDateTime;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import it.pagopa.pn.downtime.consumer.CloudwatchReceiver;
import it.pagopa.pn.downtime.consumer.LegalFactIdReceiver;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionality;

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
	
	@Test
	public void test_messageMockCloudwatchReceiver() throws Throwable {
		String messageCloudwatch = getMessageCloudwatchFromResource();
		
		mockCloudwatchReceiver.receiveMessage(messageCloudwatch);
		mockAddStatusChange_KO(client);
		
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_messageMockLegalFactIdReceiver() throws Throwable {
		
		DowntimeLogs dt = getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"), PnFunctionality.NOTIFICATION_CREATE, "EVENT", "akdocdfe-50403", OffsetDateTime.parse("2022-08-28T09:55:15.995Z"));
		dt.setFileAvailable(true);
		dt.setLegalFactId("PN_LEGAL_FACTS-TEST");
		
		mockFindFirstByLegalFactId(dt);
		mockSaveDowntime(dt);
		
		String messageLegalFactId = getMessageLegalFactIdFromResource();
		
		mockLegalFactIdReceiver.receiveLegalFact(messageLegalFactId);
		
		Assertions.assertTrue(true);
	}
	
}
