package it.pagopa.pn.downtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.downtime.consumer.CloudwatchReceiver;
import it.pagopa.pn.downtime.consumer.DowntimeLogsReceiver;
import it.pagopa.pn.downtime.consumer.LegalFactIdReceiver;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.service.LegalFactService;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class MessageSQSConsumerByClientTemplatesTest extends AbstractMock {

    @Autowired
    DowntimeLogsReceiver mockDowntimeLogsReceiver;

    @Autowired
    LegalFactService legalFactService;

    @Autowired
    ObjectMapper mapper;

    @Test
    public void test_messageMockActsQueueReceiver() throws Throwable {
        mockAddStatusChange_OK(client);
        mockTemplatesClientBehavior();
        mockSaveDowntime();
        String messageActsQueue = getMessageActsQueueFromResource();
        mockDowntimeLogsReceiver.receiveStringMessage(messageActsQueue);
        DowntimeLogs dt = mapper.readValue(messageActsQueue, DowntimeLogs.class);
        assertThat(legalFactService.generateLegalFact(dt).toString()).contains("legalFactId");
    }
}
