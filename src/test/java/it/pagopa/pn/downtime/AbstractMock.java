package it.pagopa.pn.downtime;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.pagopa.pn.downtime.dto.response.DownloadLegalFactDto;
import it.pagopa.pn.downtime.dto.response.GetLegalFactDto;
import it.pagopa.pn.downtime.dto.response.UploadSafeStorageDto;
import it.pagopa.pn.downtime.mapper.CloudwatchMapper;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime.api.DowntimeApi;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.producer.DowntimeLogsSend;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
import it.pagopa.pn.downtime.repository.EventRepository;
import it.pagopa.pn.downtime.service.DowntimeLogsService;
import it.pagopa.pn.downtime.service.LegalFactServiceImpl;

public abstract class AbstractMock {

	@Autowired
	DowntimeApi downtimeApi;
	@Autowired
	MockMvc mvc;
	@Autowired
	CloudwatchMapper cloudwatchMapper;
	@MockBean
	RestTemplate client;
	@MockBean
	protected EventRepository mockEventRepository;
	@MockBean
	protected DowntimeLogsRepository mockDowntimeLogsRepository;
	@MockBean
	private DynamoDBMapper mockDynamoDBMapper;
	@MockBean
	protected DowntimeLogsSend downtimeLogsSend;
	@Value("classpath:data/current_status.json") private Resource currentStatus;
	@Value("classpath:data/history_status.json") private Resource historyStatus;
	
	
	@InjectMocks
	protected LegalFactServiceImpl serviceLegalFact;


	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
	private static ObjectMapper mapper = new ObjectMapper();

	protected final String currentStatusUrl = "/downtime/status";
	protected final String historyStatusUrl = "/downtime/history";
	protected final String eventsUrl = "/downtime/events";
	protected final String legalFactIdUrl = "/downtime/legal-facts/";




	protected void mockFindAllByFunctionalityInAndStartDateBetween() {
		List<DowntimeLogs> downtimeLogsList = new ArrayList<>();
		downtimeLogsList
				.add(getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_CREATE, "EVENT_START", "akdoe-50403",null));
		downtimeLogsList.add(getDowntimeLogs("NOTIFICATION_VISUALIZZATION2022", OffsetDateTime.parse("2022-05-10T10:55:15.995Z"),
				PnFunctionality.NOTIFICATION_VISUALIZZATION, "EVENT_START", "akdoe-50403",null));
		Mockito.when(
				mockDowntimeLogsRepository.findAllByFunctionalityInAndStartDateBetween(
						Mockito.anyList(), Mockito.any(OffsetDateTime.class), Mockito.any(OffsetDateTime.class)))
				.thenReturn(downtimeLogsList);
	}
	
	protected void mockFindAllByFunctionalityInAndEndDateBetweenAndStartDateBefore() {
		List<DowntimeLogs> downtimeLogsList = new ArrayList<>();
		downtimeLogsList
				.add(getDowntimeLogs("NOTIFICATION_WORKFLOW2022", OffsetDateTime.parse("2022-09-27T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_WORKFLOW, "EVENT_START", "akdoe-50403",null));
		Mockito.when(
				mockDowntimeLogsRepository.findAllByFunctionalityInAndEndDateBetweenAndStartDateBefore(
						Mockito.anyList(), Mockito.any(OffsetDateTime.class), Mockito.any(OffsetDateTime.class), Mockito.any(OffsetDateTime.class)))
				.thenReturn(downtimeLogsList);
	}

	
	protected void mockFindByFunctionalityAndEndDateIsNull(DowntimeLogs downtimeLogs) {
		Mockito.when(mockDowntimeLogsRepository.findByFunctionalityAndEndDateIsNull(Mockito.any(PnFunctionality.class)))
				.thenReturn(downtimeLogs);
	}

	@SuppressWarnings("unchecked")
	protected void mockFindByFunctionalityAndStartDateLessThanEqualWithEndDate() {
		List<DowntimeLogs> list = new ArrayList<>();
		list.add(getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"),
				PnFunctionality.NOTIFICATION_CREATE, "EVENT", "akdocdfe-50403", OffsetDateTime.parse("2022-08-28T09:55:15.995Z")));

		QueryResultPage<DowntimeLogs> queryResult = new QueryResultPage<>();
		queryResult.setResults(list);
		Mockito.when(mockDynamoDBMapper.queryPage(Mockito.eq(DowntimeLogs.class),
				Mockito.any(DynamoDBQueryExpression.class))).thenReturn(queryResult);

	}
	@SuppressWarnings("unchecked")
	protected void mockFindByFunctionalityAndStartDateLessThanEqualNoEndDate() {
		List<DowntimeLogs> list = new ArrayList<>();
		list.add(getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"),
				PnFunctionality.NOTIFICATION_CREATE, "EVENT", "akdocdfe-50403", null));

		QueryResultPage<DowntimeLogs> queryResult = new QueryResultPage<>();
		queryResult.setResults(list);
		Mockito.when(mockDynamoDBMapper.queryPage(Mockito.eq(DowntimeLogs.class),
				Mockito.any(DynamoDBQueryExpression.class))).thenReturn(queryResult);

	}
	@SuppressWarnings("unchecked")
	protected void mockCurrentStatus(RestTemplate client) throws IOException {
		String mock = getStringFromResourse(currentStatus);
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class)))
				.thenReturn(response);
	}

	@SuppressWarnings("unchecked")
	protected void mockHistoryStatus(RestTemplate client) throws IOException {
		String mock = getStringFromResourse(historyStatus);
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class)))
				.thenReturn(response);
	}


	protected void mockLegalFactId(RestTemplate client) {
		DownloadLegalFactDto downloadLegalFactDto = new DownloadLegalFactDto();
		downloadLegalFactDto.setUrl("http://localhost:9090");
		GetLegalFactDto getLegalFactDto = new GetLegalFactDto();
		getLegalFactDto.setVersionId("tQ74qWG0vAywePcNc");
		getLegalFactDto.setDocumentType("PN_LEGAL_FACTS");
		getLegalFactDto.setContentType("application/pdf");
		getLegalFactDto.setContentLength(new BigDecimal(104697));
		getLegalFactDto.setDownload(downloadLegalFactDto);
		getLegalFactDto.setKey("PN_LEGAL_FACTS-0002-L83U-NGPH-WHUF-I87S");
		getLegalFactDto.setStatus("PRELOADED");
		getLegalFactDto.setResultCode("200.00");
		getLegalFactDto.setRetentionUntil("2033-07-27T00:00:00.000Z");
		getLegalFactDto.setLifecycleRule("PN_LEGAL_FACTS");
		getLegalFactDto.setChecksum("cSSf87ZqNi9Dn8lZ1cDJUDNub");

		ResponseEntity<GetLegalFactDto> responseSearch = new ResponseEntity<>(getLegalFactDto, HttpStatus.OK);
		Mockito.when(client.exchange(
				ArgumentMatchers.any(URI.class), 
				ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), 
				ArgumentMatchers.<Class<GetLegalFactDto>>any()))
				.thenReturn(responseSearch);
	}

	protected void mockLegalFactIdError(RestTemplate client) {
		Mockito.when(client.exchange(ArgumentMatchers.any(URI.class), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<Object>>any()))
				.thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "403 Forbidden: [{\"resultDescription\": \"Unauthorized\", \"errorList\": [\"client is not allowed to read doc type PROVA\"], \"resultCode\": \"403.00\"}]"));
	}

	@SuppressWarnings("unchecked")
	protected void mockAddStatusChange_KO(RestTemplate client) {
		String mock = "";
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(response);
	}


	protected void mockAddStatusChange_OK(RestTemplate client) {
		UploadSafeStorageDto uploadSafeStorageDto = new UploadSafeStorageDto();
		uploadSafeStorageDto.setUploadMethod("PUT");
		uploadSafeStorageDto.setKey("PN_LEGAL_FACTS-0002-L83U-NGPH-WHUF-I87S");
		uploadSafeStorageDto.setSecret("123930");
		uploadSafeStorageDto.setResultCode("200.00");
		uploadSafeStorageDto.setUploadUrl("http://amazon_url");
		ResponseEntity<UploadSafeStorageDto> responseUpload = new ResponseEntity<>(uploadSafeStorageDto, HttpStatus.OK);
		Mockito.when(client.exchange(
				ArgumentMatchers.anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.any(HttpEntity.class),
				ArgumentMatchers.<Class<UploadSafeStorageDto>>any()))
		.thenReturn(responseUpload);
		ResponseEntity<Object> response = new ResponseEntity<>(HttpStatus.OK);
		Mockito.when(client.exchange(
				ArgumentMatchers.any(URI.class), 
				ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), 
				ArgumentMatchers.<Class<Object>>any()))
		.thenReturn(response);
	}
	
	protected void mockHistory_BADREQUEST(RestTemplate client) {
		DowntimeLogsService serviceDowntime = Mockito.mock(DowntimeLogsService.class);
		
		Mockito.when(serviceDowntime.getStatusHistory(
				ArgumentMatchers.isNull(), 
				ArgumentMatchers.any(OffsetDateTime.class), 
				ArgumentMatchers.isNull(),
				ArgumentMatchers.anyString(), 
				ArgumentMatchers.anyString()))
		.thenThrow(new RuntimeException("La data di inzio /U+00E8 obbligatoria"));	
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
		String fromTimeString = fromTime !=null ?fromTime.toString() : "";
		String toTimeString = toTime.toString();
		if (functionality != null) {
			String functionalityString = Arrays.toString(functionality.toArray()).replace('[', ' ').replace(']', ' ')
					.trim();
			requestParams.add("functionality", functionalityString);
		}
		requestParams.add("fromTime", fromTimeString);
		requestParams.add("toTime", toTimeString);
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
			PnFunctionality functionality, String startEventUuid, String uuid ,OffsetDateTime endDate) {
		DowntimeLogs downtimeLogs = new DowntimeLogs();
		downtimeLogs.setFunctionalityStartYear(functionalityStartYear);
		downtimeLogs.setStartDate(startDate);
		downtimeLogs.setStatus(PnFunctionalityStatus.KO);
		downtimeLogs.setStartEventUuid(startEventUuid);
		downtimeLogs.setFunctionality(functionality);
		downtimeLogs.setUuid(uuid);
		downtimeLogs.setEndDate(endDate);
		return downtimeLogs;
	}
	
	private static String getStringFromResourse(Resource resource) throws IOException {
		return StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
	}

}
