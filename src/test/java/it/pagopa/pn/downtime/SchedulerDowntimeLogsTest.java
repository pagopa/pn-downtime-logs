package it.pagopa.pn.downtime;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.core.JsonProcessingException;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.producer.DowntimeLogsSend;
import it.pagopa.pn.downtime.scheduler.LegalFactIdJob;
import it.pagopa.pn.downtime.service.impl.DowntimeLogsServiceImpl;




@SpringBootTest(classes = PnDowntimeApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class SchedulerDowntimeLogsTest extends AbstractMock{

    @InjectMocks
    private LegalFactIdJob job;  
    @Mock
    DowntimeLogsServiceImpl downtimeLogsService;
    @Mock
    AmazonSQS sqs;
    @Mock
	DowntimeLogsSend producer;
	
    @Before
    public void setUp() {
    	before();
    }
    
	@Test
	public void test_CheckLegalFactId() throws SchedulerException, InterruptedException, JsonProcessingException {
		mockProducer(producer);
		List<DowntimeLogs> downtimeLogsList = new ArrayList<>();
		downtimeLogsList
				.add(getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_CREATE, "EVENT_WITHOUT_LEGAL_FACT", "akdoe-50403", OffsetDateTime.parse("2022-08-28T14:55:15.995Z")));
		mockFindAllByEndDateIsNotNullAndLegalFactIdIsNull(downtimeLogsService, downtimeLogsList);
		job.execute(null);
		Assertions.assertTrue(true);
	}
}
