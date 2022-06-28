package it.pagopa.pn.downtime;

import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.defaultanswers.ForwardsInvocations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.pagopa.pn.downtime.dto.response.DownloadLegalFactDto;
import it.pagopa.pn.downtime.dto.response.GetLegalFactDto;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.pn_downtime.api.DowntimeApi;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
import it.pagopa.pn.downtime.repository.EventRepository;
import it.pagopa.pn.downtime.service.DowntimeLogsServiceImpl;

public abstract class AbstractMock {

	@Autowired
	DowntimeApi downtimeApi;
	@Autowired
	MockMvc mvc;
	@MockBean
	RestTemplate client;
	@MockBean
	protected EventRepository mockEventRepository;
	@MockBean
	protected DowntimeLogsRepository mockDowntimeLogsRepository;
	@Mock
	private DynamoDBMapper mapperDynamoDBMapper;

	@InjectMocks
	protected DowntimeLogsServiceImpl service;

	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
	private static ObjectMapper mapper = new ObjectMapper();

	protected final String currentStatusUrl = "/downtime/status";
	protected final String historyStatusUrl = "/downtime/history";
	protected final String eventsUrl = "/downtime/events";
	protected final String legalFactIdUrl = "/downtime/legal-facts/{legalFactId}";

	protected void mockFindByFunctionality(Page<Event> returnListPage) {
		Mockito.when(mockEventRepository.findAllByFunctionality(Mockito.anyString(), Mockito.any()))
				.thenReturn(returnListPage);
	}

	protected void mockSaveEvent(Event event) {
		Mockito.when(mockEventRepository.save(Mockito.any(Event.class))).thenReturn(event);
	}

	protected void mockSaveDowntimeLogs(DowntimeLogs downtimeLogs) {
		Mockito.when(mockDowntimeLogsRepository.save(Mockito.any(DowntimeLogs.class))).thenReturn(downtimeLogs);
	}

	@SuppressWarnings("unchecked")
	protected void mockFindByFunctionalityInAndStartDateGreaterThanEqualAndEndDateLessThanEqual() {
		List<DowntimeLogs> downtimeLogsList = new ArrayList<>();
		downtimeLogsList
				.add(getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_CREATE, PnFunctionalityStatus.KO, "EVENT_START", "akdoe-50403"));

		Page<DowntimeLogs> pagedDowntimeLogs = new PageImpl(downtimeLogsList);
		Mockito.when(
				mockDowntimeLogsRepository.findAllByFunctionalityInAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
						Mockito.anyList(), Mockito.any(OffsetDateTime.class), Mockito.any(OffsetDateTime.class),
						Mockito.any(Pageable.class)))
				.thenReturn(pagedDowntimeLogs);
	}

	protected void mockFindByFunctionalityAndEndDateIsNull(DowntimeLogs downtimeLogs) {
		Mockito.when(mockDowntimeLogsRepository.findByFunctionalityAndEndDateIsNull(Mockito.any(PnFunctionality.class)))
				.thenReturn(downtimeLogs);
	}

	@SuppressWarnings("unchecked")
	protected void mockFindByFunctionalityAndStartDateLessThanEqual() {
		List<DowntimeLogs> list = new ArrayList<>();
		list.add(getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"),
				PnFunctionality.NOTIFICATION_CREATE, PnFunctionalityStatus.KO, "EVENT", "akdocdfe-50403"));
		list.add(getDowntimeLogs("NOTIFICATION_VISUALIZZATION2022", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"),
				PnFunctionality.NOTIFICATION_VISUALIZZATION, PnFunctionalityStatus.KO, "EVENT", "ADFakdocdfe-50403"));
		Mockito.when(
				mapperDynamoDBMapper.query(Mockito.eq(DowntimeLogs.class), Mockito.any(DynamoDBQueryExpression.class)))
				.thenReturn(mock(PaginatedQueryList.class,
						Mockito.withSettings().defaultAnswer(new ForwardsInvocations(list))));

	}

	@SuppressWarnings("unchecked")
	protected void mockCurrentStatus(RestTemplate client) {
		String mock = "{\"functionalities\":[\"NOTIFICATION_CREATE\",\"NOTIFICATION_VISUALIZZATION\",\"NOTIFICATION_WORKFLOW\"],\"openIncidents\":[{\"functionality\": \"NOTIFICATION_CREATE\",\"status\": \"KO\",\"startDate\": \"2022-08-24T08:55:15.995Z\",\"endDate\": null,\"legalFactId\": null}]}";
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class)))
				.thenReturn(response);
	}

	@SuppressWarnings("unchecked")
	protected void mockHistoryStatus(RestTemplate client) {
		String mock = "{\"result\":[{\"endDate\":\"2000-01-23T04:56:07.000+00:00\",\"legalFactId\":\"PN_LEGAL_FACTS-0002-DHKS-ZK03-9OAG-9OYQ\",\"startDate\":\"2000-01-23T04:56:07.000+00:00\", \"functionality\": \"NOTIFICATION_VISUALIZZATION\", \"status\": \"KO\"},"
				+ "{\"endDate\":\"2000-01-23T04:56:07.000+00:00\",\"legalFactId\":\"PN_LEGAL_FACTS-0002-L83U-NGPH-WHUF-I87S\",\"startDate\":\"2000-01-23T04:56:07.000+00:00\", \"functionality\": \"NOTIFICATION_CREATE\", \"status\": \"KO\"}],\"nextPage\": 0}";
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class)))
				.thenReturn(response);
	}

	@SuppressWarnings("unchecked")
	protected void mockLegalFactId(RestTemplate client) {
		String mock = "{\"filename\":\"filename\",\"retryAfter\": \"retryAfter\",\"contentLength\":\"contentLength\",\"url\":\"url\"}";
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		GetLegalFactDto getLegalFactDto = new GetLegalFactDto();
		DownloadLegalFactDto downloadLegalFactDto = new DownloadLegalFactDto();
		downloadLegalFactDto.setUrl("http://localhost:9090");
		getLegalFactDto.setContentLength(new BigDecimal(104697));
		getLegalFactDto.setDownload(downloadLegalFactDto);
		ResponseEntity<GetLegalFactDto> responseSearch = new ResponseEntity<GetLegalFactDto>(getLegalFactDto,
				HttpStatus.OK);
		Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<GetLegalFactDto>>any()))
				.thenReturn(responseSearch);
	}

	@SuppressWarnings("unchecked")
	protected void mockAddStatusChange_KO(RestTemplate client) {
		String mock = "";
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(response);
	}

	@SuppressWarnings("unchecked")
	protected void mockAddStatusChange_OK(RestTemplate client) {
		DowntimeLogs downtimeLogs = getDowntimeLogs("NOTIFICATION_CREATE2022",
				OffsetDateTime.parse("2022-08-28T14:55:15.995Z"), PnFunctionality.NOTIFICATION_CREATE,
				PnFunctionalityStatus.OK, "EVENTKO", "akdoe-50403");
		ResponseEntity<DowntimeLogs> responseSearch = new ResponseEntity<DowntimeLogs>(downtimeLogs, HttpStatus.OK);
		Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<DowntimeLogs>>any()))
				.thenReturn(responseSearch);
	}

	protected List<PnStatusUpdateEvent> getAddStatusChangeEventInterface() {
		List<PnFunctionality> pnFunctionality = new ArrayList<>();
		pnFunctionality.add(PnFunctionality.NOTIFICATION_CREATE);
		pnFunctionality.add(PnFunctionality.NOTIFICATION_WORKFLOW);
		List<PnStatusUpdateEvent> pnStatusUpdateEventList = new ArrayList<>();
		PnStatusUpdateEvent pnStatusUpdateEvent = new PnStatusUpdateEvent();
		pnStatusUpdateEvent.setFunctionality(pnFunctionality);
		pnStatusUpdateEvent.setTimestamp(OffsetDateTime.parse("2022-08-28T09:55:15.995Z"));
		pnStatusUpdateEvent.setSource("OPERATOR");
		pnStatusUpdateEvent.setSourceType(SourceTypeEnum.OPERATOR);
		pnStatusUpdateEvent.setStatus(PnFunctionalityStatus.OK);
		pnStatusUpdateEventList.add(pnStatusUpdateEvent);

		return pnStatusUpdateEventList;
	}

	protected static LinkedMultiValueMap<String, String> getMockHistoryStatus(OffsetDateTime fromTime,
			OffsetDateTime toTime, List<PnFunctionality> functionality, String page, String size)
			throws JsonProcessingException {
		LinkedMultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
		String fromTimeString = fromTime.toString();
		String toTimeString = toTime.toString();
		String functionalityString = Arrays.toString(functionality.toArray()).replace('[', ' ').replace(']', ' ')
				.trim();
		requestParams.add("fromTime", fromTimeString);
		requestParams.add("toTime", toTimeString);
		requestParams.add("functionality", functionalityString);
		requestParams.add("page", page);
		requestParams.add("size", size);
		return requestParams;
	}

	protected static String getPnStatusUpdateEvent(OffsetDateTime timestamp, List<PnFunctionality> functionality,
			PnFunctionalityStatus status, SourceTypeEnum sourceType, String source) throws JsonProcessingException {
		PnStatusUpdateEvent event = new PnStatusUpdateEvent();
		List<PnStatusUpdateEvent> events = new ArrayList<>();
		event.setTimestamp(timestamp);
		event.setStatus(status);
		event.setSourceType(sourceType);
		event.setSource(source);
		event.setFunctionality(functionality);
		events.add(event);

		return mapper.registerModule(new JavaTimeModule()).writeValueAsString(events);
	}

	protected static DowntimeLogs getDowntimeLogs(String functionalityStartYear, OffsetDateTime startDate,
			PnFunctionality functionality, PnFunctionalityStatus status, String startEventUuid, String uuid) {
		DowntimeLogs downtimeLogs = new DowntimeLogs();
		downtimeLogs.setFunctionalityStartYear(functionalityStartYear);
		downtimeLogs.setStartDate(startDate);
		downtimeLogs.setStatus(status);
		downtimeLogs.setStartEventUuid(startEventUuid);
		downtimeLogs.setFunctionality(functionality);
		downtimeLogs.setUuid(uuid);
		return downtimeLogs;
	}

}
