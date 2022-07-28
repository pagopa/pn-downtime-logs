package it.pagopa.pn.downtime;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.producer.DowntimeLogsSend;
import it.pagopa.pn.downtime.scheduler.LegalFactIdJob;




@SpringBootTest(classes = PnDowntimeApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)

public class TestSchedulerDowntimeLogs extends AbstractMock{

    @Autowired
    private LegalFactIdJob job;
    
    @MockBean
	DowntimeLogsSend producer;
    
	@Value("${amazon.sqs.end-point.attidagenerare}")
	private String url;
	
    @Before
    public void setUp() {
    	before();
    }
    
	@Test
	public void test_CheckLegalFactId() throws SchedulerException, InterruptedException {
		List<DowntimeLogs> downtimeLogsList = new ArrayList<>();
		downtimeLogsList
				.add(getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_CREATE, "EVENT_WITHOUT_LEGAL_FACT", "akdoe-50403", OffsetDateTime.parse("2022-08-28T14:55:15.995Z")));
		mockFindAllByEndDateIsNotNullAndLegalFactIdIsNull(downtimeLogsList);
		getMockRestGetForEntity(DowntimeLogs.class, url, getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T13:55:15.995Z"),
				PnFunctionality.NOTIFICATION_CREATE, "EVENT_WITHOUT_LEGAL_FACT", "akdoe-50403", OffsetDateTime.parse("2022-08-28T14:55:15.995Z")), HttpStatus.OK);
		job.execute(null);
		Assertions.assertTrue(true);
	}
}
