package it.pagopa.pn.downtime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionalityStatus;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class DowntimeLogsRepositoryTest {

	@MockBean
	private DynamoDbTable<DowntimeLogs> downtimeLogsTable;

	@MockBean
	private DynamoDbTable<Event> eventTable;

	@Autowired
	private DowntimeLogsRepository downtimeLogsRepository;

	List<DowntimeLogs> downtimeLogsListExpected;

	OffsetDateTime searchParameter = OffsetDateTime.parse("2022-09-27T13:55:15.995Z");

	@BeforeEach
	void setUp() {
		downtimeLogsListExpected = List
				.of(getDowntimeLogs("NOTIFICATION_WORKFLOW2022", OffsetDateTime.parse("2022-09-28T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_WORKFLOW, "EVENT_START", "akdoe-50403", null));
	}

	@SuppressWarnings("unchecked")
	private PageIterable<DowntimeLogs> stubPageIterable(List<DowntimeLogs> items) {
		PageIterable<DowntimeLogs> pi = mock(PageIterable.class);
		SdkIterable<DowntimeLogs> si = new SdkIterable<DowntimeLogs>() {
			@Override public Iterator<DowntimeLogs> iterator() { return items.iterator(); }
		};
		Mockito.when(pi.items()).thenReturn(si);
		return pi;
	}

	@Test
	void mockFindOpenDowntimeLogsFuture() {
		PageIterable<DowntimeLogs> pi = stubPageIterable(downtimeLogsListExpected);
		Mockito.when(downtimeLogsTable.query(any(QueryEnhancedRequest.class))).thenReturn(pi);

		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findOpenDowntimeLogsFuture(searchParameter, PnFunctionality.NOTIFICATION_CREATE, searchParameter);

		assertTrue(resultQuery.isPresent());
		assertTrue(resultQuery.get().getStartDate().isAfter(searchParameter));
		assertNull(resultQuery.get().getEndDate());
	}

	@Test
	void mockFindOpenDowntimeLogsFuture_ReturnEmptyList() {
		PageIterable<DowntimeLogs> pi = stubPageIterable(List.of());
		Mockito.when(downtimeLogsTable.query(any(QueryEnhancedRequest.class))).thenReturn(pi);

		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findOpenDowntimeLogsFuture(searchParameter, PnFunctionality.NOTIFICATION_CREATE, searchParameter);

		assertTrue(resultQuery.isEmpty());
	}

	@Test
	void mockFindDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists() {
		downtimeLogsListExpected = List
				.of(getDowntimeLogs("NOTIFICATION_WORKFLOW2022", OffsetDateTime.parse("2022-09-26T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_WORKFLOW, "EVENT_START", "akdoe-50403", OffsetDateTime.parse("2022-09-28T13:55:15.995Z")));
		PageIterable<DowntimeLogs> pi = stubPageIterable(downtimeLogsListExpected);
		Mockito.when(downtimeLogsTable.query(any(QueryEnhancedRequest.class))).thenReturn(pi);

		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists(searchParameter, PnFunctionality.NOTIFICATION_CREATE, searchParameter);

		assertTrue(resultQuery.isPresent());
		assertNotNull(resultQuery.get().getEndDate());
		assertTrue(resultQuery.get().getStartDate().isBefore(searchParameter) && resultQuery.get().getEndDate().isAfter(searchParameter));
	}

	@Test
	void mockFindDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists_ReturnEmptyList() {
		PageIterable<DowntimeLogs> pi = stubPageIterable(List.of());
		Mockito.when(downtimeLogsTable.query(any(QueryEnhancedRequest.class))).thenReturn(pi);

		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists(searchParameter, PnFunctionality.NOTIFICATION_CREATE, searchParameter);

		assertTrue(resultQuery.isEmpty());
	}

	@Test
	void mockFindLastDowntimeLogsWithoutEndDate_whenGivenDowntimeLogsListNotEmpty() {
		downtimeLogsListExpected = List
				.of(getDowntimeLogs("NOTIFICATION_WORKFLOW2022", OffsetDateTime.parse("2022-09-26T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_WORKFLOW, "EVENT_START", "akdoe-50403", null));
		PageIterable<DowntimeLogs> pi = stubPageIterable(downtimeLogsListExpected);
		Mockito.when(downtimeLogsTable.query(any(QueryEnhancedRequest.class))).thenReturn(pi);

		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findLastDowntimeLogsWithoutEndDate(searchParameter, PnFunctionality.NOTIFICATION_CREATE, searchParameter);

		assertTrue(resultQuery.isPresent());
		assertNull(resultQuery.get().getEndDate());
		assertTrue(resultQuery.get().getStartDate().isBefore(searchParameter));
	}

	@Test
	void mockFindLastDowntimeLogsWithoutEndDate_ReturnEmptyList() {
		PageIterable<DowntimeLogs> pi = stubPageIterable(List.of());
		Mockito.when(downtimeLogsTable.query(any(QueryEnhancedRequest.class))).thenReturn(pi);

		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findLastDowntimeLogsWithoutEndDate(searchParameter, PnFunctionality.NOTIFICATION_CREATE, searchParameter);

		assertTrue(resultQuery.isEmpty());
	}

	@Test
	@SuppressWarnings("unchecked")
	void mockFindLastDowntimeLogs_whenGivenDowntimeLogsListNotEmpty() {
		downtimeLogsListExpected = List
				.of(getDowntimeLogs("NOTIFICATION_WORKFLOW2022", OffsetDateTime.parse("2022-09-26T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_WORKFLOW, "EVENT_START", "akdoe-50403", OffsetDateTime.parse("2022-09-26T18:55:15.995Z")));

		PageIterable<DowntimeLogs> empty = stubPageIterable(List.of());
		PageIterable<DowntimeLogs> result = stubPageIterable(downtimeLogsListExpected);
		Mockito.when(downtimeLogsTable.query(any(QueryEnhancedRequest.class)))
				.thenReturn(empty, result);

		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findLastDowntimeLogsWithoutEndDate(searchParameter, PnFunctionality.NOTIFICATION_CREATE, searchParameter);

		assertTrue(resultQuery.isPresent());
		assertNotNull(resultQuery.get().getEndDate());
	}

	@Test
	void mockFindNextDowntimeLogs_whenGivenDowntimeLogsListNotEmpty() {
		downtimeLogsListExpected = List
				.of(getDowntimeLogs("NOTIFICATION_WORKFLOW2022", OffsetDateTime.parse("2022-09-28T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_WORKFLOW, "EVENT_START", "akdoe-50403", OffsetDateTime.parse("2022-09-29T18:55:15.995Z")));

		PageIterable<DowntimeLogs> pi = stubPageIterable(downtimeLogsListExpected);
		Mockito.when(downtimeLogsTable.query(any(QueryEnhancedRequest.class))).thenReturn(pi);

		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findNextDowntimeLogs(searchParameter, PnFunctionality.NOTIFICATION_CREATE, searchParameter);

		assertTrue(resultQuery.isPresent());
		assertTrue(resultQuery.get().getStartDate().isAfter(searchParameter));
	}

	@Test
	void mockFindNextDowntimeLogs_ReturnEmptyList() {
		PageIterable<DowntimeLogs> pi = stubPageIterable(List.of());
		Mockito.when(downtimeLogsTable.query(any(QueryEnhancedRequest.class))).thenReturn(pi);

		Optional<DowntimeLogs> resultQuery = downtimeLogsRepository.findNextDowntimeLogs(searchParameter, PnFunctionality.NOTIFICATION_CREATE, searchParameter);

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
