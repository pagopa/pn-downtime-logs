package it.pagopa.pn.downtime;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.context.ActiveProfiles;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionalityStatus;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.model.converter.OffsetDateTimeConverter;
import it.pagopa.pn.downtime.model.converter.PnFunctionalityConverter;
import it.pagopa.pn.downtime.model.converter.PnFunctionalityStatusConverter;
import it.pagopa.pn.downtime.model.converter.PnSourceTypeConverter;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class DowntimeLogsRepositoryTest {

	@MockitoBean
	private DynamoDbTable<DowntimeLogs> downtimeLogsTable;

	@MockitoBean
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

	// ── Converter tests (T-01 → T-15) ────────────────────────────────────────

	@Test
	void offsetDateTime_transformFrom_nonNull() {
		OffsetDateTimeConverter.Converter converter = new OffsetDateTimeConverter.Converter();
		OffsetDateTime dt = OffsetDateTime.parse("2024-01-15T10:00:00+00:00");

		assertThat(converter.transformFrom(dt).s()).isEqualTo(dt.toString());
	}

	@Test
	void offsetDateTime_transformFrom_null() {
		OffsetDateTimeConverter.Converter converter = new OffsetDateTimeConverter.Converter();

		assertThat(converter.transformFrom(null).s()).isEmpty();
	}

	@Test
	void offsetDateTime_transformTo_validIsoString() {
		OffsetDateTimeConverter.Converter converter = new OffsetDateTimeConverter.Converter();
		String iso = "2024-01-15T10:00:00+00:00";

		assertThat(converter.transformTo(AttributeValue.builder().s(iso).build()))
				.isNotNull()
				.isEqualTo(OffsetDateTime.parse(iso));
	}

	@Test
	void offsetDateTime_transformTo_emptyString() {
		OffsetDateTimeConverter.Converter converter = new OffsetDateTimeConverter.Converter();

		assertThat(converter.transformTo(AttributeValue.builder().s("").build())).isNull();
	}

	@Test
	void offsetDateTime_transformTo_noStringValue() {
		OffsetDateTimeConverter.Converter converter = new OffsetDateTimeConverter.Converter();

		assertThat(converter.transformTo(AttributeValue.builder().build())).isNull();
	}

	@Test
	void offsetDateTime_type_andAttributeValueType() {
		OffsetDateTimeConverter.Converter converter = new OffsetDateTimeConverter.Converter();

		assertThat(converter.type()).isEqualTo(EnhancedType.of(OffsetDateTime.class));
		assertThat(converter.attributeValueType()).isEqualTo(AttributeValueType.S);
	}

	@Test
	void pnFunctionality_transformFrom() {
		PnFunctionalityConverter.Converter converter = new PnFunctionalityConverter.Converter();

		assertThat(converter.transformFrom(PnFunctionality.NOTIFICATION_CREATE).s())
				.isEqualTo(PnFunctionality.NOTIFICATION_CREATE.getValue());
	}

	@Test
	void pnFunctionality_transformTo() {
		PnFunctionalityConverter.Converter converter = new PnFunctionalityConverter.Converter();

		assertThat(converter.transformTo(AttributeValue.builder().s(PnFunctionality.NOTIFICATION_WORKFLOW.getValue()).build()))
				.isEqualTo(PnFunctionality.NOTIFICATION_WORKFLOW);
	}

	@Test
	void pnFunctionality_type_andAttributeValueType() {
		PnFunctionalityConverter.Converter converter = new PnFunctionalityConverter.Converter();

		assertThat(converter.type()).isEqualTo(EnhancedType.of(PnFunctionality.class));
		assertThat(converter.attributeValueType()).isEqualTo(AttributeValueType.S);
	}

	@Test
	void pnFunctionalityStatus_transformFrom() {
		PnFunctionalityStatusConverter.Converter converter = new PnFunctionalityStatusConverter.Converter();

		assertThat(converter.transformFrom(PnFunctionalityStatus.OK).s()).isEqualTo("OK");
		assertThat(converter.transformFrom(PnFunctionalityStatus.KO).s()).isEqualTo("KO");
	}

	@Test
	void pnFunctionalityStatus_transformTo() {
		PnFunctionalityStatusConverter.Converter converter = new PnFunctionalityStatusConverter.Converter();

		assertThat(converter.transformTo(AttributeValue.builder().s("OK").build())).isEqualTo(PnFunctionalityStatus.OK);
		assertThat(converter.transformTo(AttributeValue.builder().s("KO").build())).isEqualTo(PnFunctionalityStatus.KO);
	}

	@Test
	void pnFunctionalityStatus_type_andAttributeValueType() {
		PnFunctionalityStatusConverter.Converter converter = new PnFunctionalityStatusConverter.Converter();

		assertThat(converter.type()).isEqualTo(EnhancedType.of(PnFunctionalityStatus.class));
		assertThat(converter.attributeValueType()).isEqualTo(AttributeValueType.S);
	}

	@Test
	void pnSourceType_transformFrom() {
		PnSourceTypeConverter.Converter converter = new PnSourceTypeConverter.Converter();

		assertThat(converter.transformFrom(SourceTypeEnum.ALARM).s()).isEqualTo(SourceTypeEnum.ALARM.getValue());
	}

	@Test
	void pnSourceType_transformTo() {
		PnSourceTypeConverter.Converter converter = new PnSourceTypeConverter.Converter();

		assertThat(converter.transformTo(AttributeValue.builder().s(SourceTypeEnum.ALARM.getValue()).build()))
				.isEqualTo(SourceTypeEnum.ALARM);
	}

	@Test
	void pnSourceType_type_andAttributeValueType() {
		PnSourceTypeConverter.Converter converter = new PnSourceTypeConverter.Converter();

		assertThat(converter.type()).isEqualTo(EnhancedType.of(SourceTypeEnum.class));
		assertThat(converter.attributeValueType()).isEqualTo(AttributeValueType.S);
	}
}
