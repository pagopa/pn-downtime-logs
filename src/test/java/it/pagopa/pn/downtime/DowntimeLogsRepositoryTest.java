package it.pagopa.pn.downtime;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;

import io.awspring.cloud.messaging.listener.SimpleMessageListenerContainer;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;

@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class DowntimeLogsRepositoryTest {

	@MockBean
	SimpleMessageListenerContainer simpleMessageListenerContainer;
	
	@MockBean
	private DynamoDBMapper mapper;

	@Autowired
	private DowntimeLogsRepository downtimeLogsRepository;
	
	List<DowntimeLogs> downtimeLogsListExpected; 
	
	OffsetDateTime searchParameter = OffsetDateTime.parse("2022-09-27T13:55:15.995Z");
	
	@Test
	void mockFindOpenDowntimeLogsFuture() {
		downtimeLogsListExpected = List
				.of(getDowntimeLogs("NOTIFICATION_WORKFLOW2022", OffsetDateTime.parse("2022-09-28T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_WORKFLOW, "EVENT_START", "akdoe-50403", null));
		QueryResultPage<DowntimeLogs> queryResultPage = new QueryResultPage<DowntimeLogs>();
		queryResultPage.setResults(downtimeLogsListExpected);
		Mockito.when(mapper.queryPage(ArgumentMatchers.eq(DowntimeLogs.class), ArgumentMatchers.<DynamoDBQueryExpression<DowntimeLogs>>any())).thenReturn(queryResultPage);
		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findOpenDowntimeLogsFuture(searchParameter, PnFunctionality.NOTIFICATION_CREATE, searchParameter);
		
		assertTrue(resultQuery.isPresent());
		assertTrue(resultQuery.get().getStartDate().isAfter(searchParameter));
		assertNull(resultQuery.get().getEndDate());
	}
	
	@Test
	void  mockFindOpenDowntimeLogsFuture_ReturnEmptyList() {
		Mockito.when(mapper.queryPage(ArgumentMatchers.eq(DowntimeLogs.class), ArgumentMatchers.<DynamoDBQueryExpression<DowntimeLogs>>any())).thenReturn(null);
		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findOpenDowntimeLogsFuture(OffsetDateTime.parse("2022-09-27T13:55:15.995Z"), PnFunctionality.NOTIFICATION_CREATE, OffsetDateTime.parse("2022-09-27T13:55:15.995Z"));
		
		assertTrue(resultQuery.isEmpty());
	}
	
	@Test
	void mockFindDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists() {
		downtimeLogsListExpected = List
				.of(getDowntimeLogs("NOTIFICATION_WORKFLOW2022", OffsetDateTime.parse("2022-09-26T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_WORKFLOW, "EVENT_START", "akdoe-50403", OffsetDateTime.parse("2022-09-28T13:55:15.995Z")));
		QueryResultPage<DowntimeLogs> queryResultPage = new QueryResultPage<DowntimeLogs>();
		queryResultPage.setResults(downtimeLogsListExpected);
		Mockito.when(mapper.queryPage(ArgumentMatchers.eq(DowntimeLogs.class), ArgumentMatchers.<DynamoDBQueryExpression<DowntimeLogs>>any())).thenReturn(queryResultPage);
		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists(OffsetDateTime.parse("2022-09-27T13:55:15.995Z"), PnFunctionality.NOTIFICATION_CREATE, OffsetDateTime.parse("2022-09-27T13:55:15.995Z"));
		
		assertTrue(resultQuery.isPresent());
		assertNotNull(resultQuery.get().getEndDate());
		assertTrue(resultQuery.get().getStartDate().isBefore(searchParameter) && resultQuery.get().getEndDate().isAfter(searchParameter));
	}
	
	@Test
	void  mockFindDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists_ReturnEmptyList() {
		Mockito.when(mapper.queryPage(ArgumentMatchers.eq(DowntimeLogs.class), ArgumentMatchers.<DynamoDBQueryExpression<DowntimeLogs>>any())).thenReturn(null);
		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists(OffsetDateTime.parse("2022-09-27T13:55:15.995Z"), PnFunctionality.NOTIFICATION_CREATE, OffsetDateTime.parse("2022-09-27T13:55:15.995Z"));
		
		assertTrue(resultQuery.isEmpty());
	}
	
	@Test
	void mockFindLastDowntimeLogsWithoutEndDate() {
		downtimeLogsListExpected = List
				.of(getDowntimeLogs("NOTIFICATION_WORKFLOW2022", OffsetDateTime.parse("2022-09-26T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_WORKFLOW, "EVENT_START", "akdoe-50403", null));
		QueryResultPage<DowntimeLogs> queryResultPage = new QueryResultPage<DowntimeLogs>();
		queryResultPage.setResults(downtimeLogsListExpected);
		Mockito.when(mapper.queryPage(ArgumentMatchers.eq(DowntimeLogs.class), ArgumentMatchers.<DynamoDBQueryExpression<DowntimeLogs>>any())).thenReturn(queryResultPage);
		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findLastDowntimeLogs(OffsetDateTime.parse("2022-09-27T13:55:15.995Z"), PnFunctionality.NOTIFICATION_CREATE, OffsetDateTime.parse("2022-09-27T13:55:15.995Z"));
		
		assertTrue(resultQuery.isPresent());
		assertNull(resultQuery.get().getEndDate());
		assertTrue(resultQuery.get().getStartDate().isBefore(searchParameter));
	}
	
	@Test
	void mockFindLastDowntimeLogs_ReturnEmptyList() {
		Mockito.when(mapper.queryPage(ArgumentMatchers.eq(DowntimeLogs.class), ArgumentMatchers.<DynamoDBQueryExpression<DowntimeLogs>>any())).thenReturn(null);
		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findLastDowntimeLogs(OffsetDateTime.parse("2022-09-27T13:55:15.995Z"), PnFunctionality.NOTIFICATION_CREATE, OffsetDateTime.parse("2022-09-27T13:55:15.995Z"));
		
		assertTrue(resultQuery.isEmpty());
	}
	
	protected static DowntimeLogs getDowntimeLogs(String functionalityStartYear, OffsetDateTime startDate,
			PnFunctionality functionality, String startEventUuid, String uuid, OffsetDateTime endDate) {
		DowntimeLogs downtimeLogs = new DowntimeLogs();
		downtimeLogs.setFunctionalityStartYear(functionalityStartYear);
		downtimeLogs.setStartDate(startDate);
		downtimeLogs.setStatus(PnFunctionalityStatus.KO);
		downtimeLogs.setStartEventUuid(startEventUuid);
		downtimeLogs.setFunctionality(functionality);
		downtimeLogs.setUuid(uuid);
		downtimeLogs.setEndDate(endDate);
		downtimeLogs.setFileAvailable(false);
		downtimeLogs.setHistory("downtimeHistory");
		return downtimeLogs;
	}

}
