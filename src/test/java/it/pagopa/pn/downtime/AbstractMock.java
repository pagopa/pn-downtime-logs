package it.pagopa.pn.downtime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.defaultanswers.ForwardsInvocations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedParallelScanList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.ScanResultPage;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.awspring.cloud.messaging.listener.SimpleMessageListenerContainer;
import it.pagopa.pn.downtime.mapper.CloudwatchMapper;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.pn_downtime_logs.restclient.safestorage.api.FileDownloadApi;
import it.pagopa.pn.downtime.pn_downtime_logs.restclient.safestorage.api.FileUploadApi;
import it.pagopa.pn.downtime.pn_downtime_logs.restclient.safestorage.model.FileCreationResponse;
import it.pagopa.pn.downtime.pn_downtime_logs.restclient.safestorage.model.FileCreationResponse.UploadMethodEnum;
import it.pagopa.pn.downtime.pn_downtime_logs.restclient.safestorage.model.FileDownloadInfo;
import it.pagopa.pn.downtime.pn_downtime_logs.restclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.downtime.producer.DowntimeLogsSend;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
import it.pagopa.pn.downtime.service.DowntimeLogsService;
import it.pagopa.pn.downtime.service.impl.DowntimeLogsServiceImpl;

public abstract class AbstractMock {

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	@Autowired
	MockMvc mvc;

	@Autowired
	CloudwatchMapper cloudwatchMapper;

	@MockBean
	@Qualifier("restTemplate")
	RestTemplate client;

	@MockBean
	protected DynamoDBMapper mockDynamoDBMapper;

	@MockBean
	protected DowntimeLogsRepository mockDowntimeLogsRepository;

	@MockBean
	SimpleMessageListenerContainer simpleMessageListenerContainer;

	@Autowired
	protected DowntimeLogsServiceImpl service;

	@MockBean
	private FileDownloadApi fileDownloadApi;

	@MockBean
	private FileUploadApi fileUploadApi;

	@Value("classpath:data/current_status.json")
	private Resource currentStatus;
	@Value("classpath:data/current_status500.json")
	private Resource currentStatus500;
	@Value("classpath:data/history_status.json")
	private Resource historyStatus;
	@Value("classpath:data/messageCloudwatch.json")
	private Resource mockMessageCloudwatch;
	@Value("classpath:data/messageLegalFactId.json")
	private Resource mockMessageLegalFactId;
	@Value("classpath:data/message_acts_queue.json")
	private Resource mockMessageActsQueue;

	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	private static ObjectMapper mapper = new ObjectMapper();

	protected final String currentStatusUrl = "/downtime/v1/status";
	protected final String statusUrl = "/status";
	protected final String historyStatusUrl = "/downtime/v1/history";
	protected final String eventsUrl = "/downtime-internal/v1/events";
	protected final String legalFactIdUrl = "/downtime/v1/legal-facts/";
	protected final String healthCheckUrl = "/healthcheck";

	protected void mockProducer(DowntimeLogsSend producer) throws JsonProcessingException {
		Mockito.doNothing().when(producer).sendMessage(Mockito.any(), Mockito.anyString());
	}

	@SuppressWarnings("unchecked")
	protected void mockFindByFunctionalityInAndStartDateBetween() {
		List<DowntimeLogs> downtimeLogsList = new ArrayList<>();
		downtimeLogsList
				.add(getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_CREATE, "EVENT_START", "akdoe-50403", null));
		downtimeLogsList.add(
				getDowntimeLogs("NOTIFICATION_VISUALIZZATION2022", OffsetDateTime.parse("2022-05-10T10:55:15.995Z"),
						PnFunctionality.NOTIFICATION_VISUALIZATION, "EVENT_START", "akdoe-50403", null));

		Mockito.when(mockDynamoDBMapper.query(ArgumentMatchers.eq(DowntimeLogs.class),
				ArgumentMatchers.<DynamoDBQueryExpression<DowntimeLogs>>any()))
				.thenReturn(mock(PaginatedQueryList.class,
						withSettings().defaultAnswer(new ForwardsInvocations(downtimeLogsList))));
	}

	@SuppressWarnings("unchecked")
	protected void mockFindAllByEndDateIsNotNullAndLegalFactIdIsNull(DowntimeLogsService downtimeLogsService,
			List<DowntimeLogs> downtimeLogsList) {
		Mockito.when(mockDynamoDBMapper.query(ArgumentMatchers.eq(DowntimeLogs.class),
				ArgumentMatchers.<DynamoDBQueryExpression<DowntimeLogs>>any()))
				.thenReturn(mock(PaginatedQueryList.class,
						withSettings().defaultAnswer(new ForwardsInvocations(downtimeLogsList))));
	}

	@SuppressWarnings("unchecked")
	protected void mockFindAllByEndDateIsNotNullAndLegalFactIdIsNullWithParallelScan(
			DowntimeLogsService downtimeLogsService, List<DowntimeLogs> downtimeLogsList) {
		Mockito.when(mockDynamoDBMapper.parallelScan(ArgumentMatchers.<Class<DowntimeLogs>>any(), Mockito.any(),
				Mockito.anyInt()))
				.thenReturn(mock(PaginatedParallelScanList.class,
						withSettings().defaultAnswer(new ForwardsInvocations(downtimeLogsList))));
	}

	@SuppressWarnings("unchecked")
	protected void mockFindAllByFunctionalityInAndEndDateBetweenAndStartDateBefore() {
		List<DowntimeLogs> downtimeLogsList = new ArrayList<>();
		downtimeLogsList.add(getDowntimeLogs("NOTIFICATION_WORKFLOW2022", OffsetDateTime.parse("2022-09-27T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_WORKFLOW, "EVENT_START", "akdoe-50403", null));

		Mockito.when(
				mockDynamoDBMapper.query(Mockito.eq(DowntimeLogs.class), Mockito.any(DynamoDBQueryExpression.class)))
				.thenReturn(mock(PaginatedQueryList.class,
						withSettings().defaultAnswer(new ForwardsInvocations(downtimeLogsList))));
	}

	protected void mockFindFirstByLegalFactId(DowntimeLogs downtimeLogs) {
		ScanResultPage<DowntimeLogs> scanPageDowntime = new ScanResultPage<>();
		scanPageDowntime.setResults(List.of(downtimeLogs));
		Mockito.when(mockDynamoDBMapper.scanPage(ArgumentMatchers.<Class<DowntimeLogs>>any(), Mockito.any()))
				.thenReturn(scanPageDowntime);
	}

	protected void mockSaveDowntime() {
		Mockito.doNothing().when(mockDynamoDBMapper).save(Mockito.any(DowntimeLogs.class));
	}

	protected void mockSaveEvent() {
		Mockito.doNothing().when(mockDynamoDBMapper).save(Mockito.any(Event.class));
	}

	@SuppressWarnings("unchecked")
	protected void mockFindAllByFunctionalityInAndStartDateAfter() {
		List<DowntimeLogs> listDowntime = new ArrayList<>();

		listDowntime
				.add(getDowntimeLogs("NOTIFICATION_WORKFLOW2022", OffsetDateTime.parse("2022-02-24T08:56:07.000+00:00"),
						PnFunctionality.NOTIFICATION_WORKFLOW, "PAGO-PA-EVENT-W", "123", null));
		listDowntime.add(getDowntimeLogs("NOTIFICATION_VISUALIZZATION2022",
				OffsetDateTime.parse("2022-01-28T04:56:07.000+00:00"), PnFunctionality.NOTIFICATION_VISUALIZATION,
				"PAGO-PA-EVENT-V", "12345", OffsetDateTime.parse("2022-01-30T04:56:07.000+00:00")));
		listDowntime.add(getDowntimeLogs("NOTIFICATION_CREATE2022",
				OffsetDateTime.parse("2022-01-25T04:56:07.000+00:00"), PnFunctionality.NOTIFICATION_CREATE,
				"PAGO-PA-EVENT-C", "10DJAKDF", OffsetDateTime.parse("2022-01-25T10:56:07.000+00:00")));

		Mockito.when(
				mockDynamoDBMapper.query(Mockito.eq(DowntimeLogs.class), Mockito.any(DynamoDBQueryExpression.class)))
				.thenReturn(mock(PaginatedQueryList.class,
						withSettings().defaultAnswer(new ForwardsInvocations(listDowntime))));
	}

	@SuppressWarnings("unchecked")
	protected void mockFindAllByFunctionalityInAndEndDateAfterAndStartDateBefore() {
		List<DowntimeLogs> listDowntime = new ArrayList<>();
		listDowntime.add(getDowntimeLogs("NOTIFICATION_WORKFLOW2022",
				OffsetDateTime.parse("2022-01-19T04:56:07.000+00:00"), PnFunctionality.NOTIFICATION_WORKFLOW,
				"PAGO-PA-EVENT", "123", OffsetDateTime.parse("2022-01-23T10:56:07.000+00:00")));
		listDowntime.add(getDowntimeLogs("NOTIFICATION_WORKFLOW2022",
				OffsetDateTime.parse("2022-01-20T04:56:07.000+00:00"), PnFunctionality.NOTIFICATION_WORKFLOW,
				"PAGO-PA-EVENT", "123", OffsetDateTime.parse("2022-02-23T04:56:07.000+00:00")));
		listDowntime.add(getDowntimeLogs("NOTIFICATION_WORKFLOW2022",
				OffsetDateTime.parse("2022-01-23T02:56:07.000+00:00"), PnFunctionality.NOTIFICATION_WORKFLOW,
				"PAGO-PA-EVENT", "123", OffsetDateTime.parse("2022-01-30T04:56:07.000+00:00")));
		Mockito.when(
				mockDynamoDBMapper.query(Mockito.eq(DowntimeLogs.class), Mockito.any(DynamoDBQueryExpression.class)))
				.thenReturn(mock(PaginatedQueryList.class,
						withSettings().defaultAnswer(new ForwardsInvocations(listDowntime))));
	}

	protected void mockStatusError() {
		Mockito.when(mockDynamoDBMapper.parallelScan(ArgumentMatchers.<Class<DowntimeLogs>>any(), Mockito.any(),
				Mockito.anyInt())).thenThrow(new RuntimeException());
	}

	@SuppressWarnings("unchecked")
	protected void mockFindByFunctionalityAndEndDateIsNull(DowntimeLogs downtimeLogs) {
		Mockito.when(
				mockDynamoDBMapper.query(Mockito.eq(DowntimeLogs.class), Mockito.any(DynamoDBQueryExpression.class)))
				.thenReturn(mock(PaginatedQueryList.class,
						withSettings().defaultAnswer(new ForwardsInvocations(List.of(downtimeLogs)))));
	}

	@SuppressWarnings("unchecked")
	protected void mockFindByFunctionalityAndEndDateIsNullCheck500(DowntimeLogs downtimeLogs) {
		List<DowntimeLogs> listDowntimeLogs = new ArrayList<>();
		listDowntimeLogs.add(downtimeLogs);
		Mockito.when(mockDynamoDBMapper.parallelScan(ArgumentMatchers.<Class<DowntimeLogs>>any(), Mockito.any(),
				Mockito.anyInt()))
				.thenReturn(mock(PaginatedParallelScanList.class,
						withSettings().defaultAnswer(new ForwardsInvocations(listDowntimeLogs))));
	}

	@SuppressWarnings("unchecked")
	protected void mockFindByFunctionalityAndStartDateLessThanEqualNoEndDate() {
		Mockito.when(mockDowntimeLogsRepository.findOpenDowntimeLogsFuture(ArgumentMatchers.any(OffsetDateTime.class),
				ArgumentMatchers.any(PnFunctionality.class), ArgumentMatchers.any(OffsetDateTime.class)))
				.thenReturn(Optional.empty());

		Mockito.when(mockDowntimeLogsRepository.findDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists(
				ArgumentMatchers.any(OffsetDateTime.class), ArgumentMatchers.any(PnFunctionality.class),
				ArgumentMatchers.any(OffsetDateTime.class))).thenReturn(Optional.empty());

		Optional<DowntimeLogs> optionalDowntimeLogs = Optional
				.of(getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"),
						PnFunctionality.NOTIFICATION_CREATE, "EVENT", "akdocdfe-50403", null));

		Mockito.when(mockDowntimeLogsRepository.findLastDowntimeLogsWithoutEndDate(ArgumentMatchers.any(OffsetDateTime.class),
				ArgumentMatchers.any(PnFunctionality.class), ArgumentMatchers.any(OffsetDateTime.class)))
				.thenReturn(Optional.empty(), optionalDowntimeLogs);

	}
	
	protected void mockFindOpenDowntimeFuture() {
		Optional<DowntimeLogs> optionalDowntimeLogs = Optional
				.of(getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"),
						PnFunctionality.NOTIFICATION_CREATE, "EVENT", "akdocdfe-50403", null));
		
		Mockito.when(mockDowntimeLogsRepository.findOpenDowntimeLogsFuture(ArgumentMatchers.any(OffsetDateTime.class),
				ArgumentMatchers.any(PnFunctionality.class), ArgumentMatchers.any(OffsetDateTime.class)))
				.thenReturn(optionalDowntimeLogs);
	}
	
	protected void mockFindDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists() {
		Optional<DowntimeLogs> optionalDowntimeLogs = Optional
				.of(getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"),
						PnFunctionality.NOTIFICATION_CREATE, "EVENT", "akdocdfe-50403", null));
		
		Mockito.when(mockDowntimeLogsRepository.findOpenDowntimeLogsFuture(ArgumentMatchers.any(OffsetDateTime.class),
				ArgumentMatchers.any(PnFunctionality.class), ArgumentMatchers.any(OffsetDateTime.class)))
				.thenReturn(Optional.empty());

		Mockito.when(mockDowntimeLogsRepository.findDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists(
				ArgumentMatchers.any(OffsetDateTime.class), ArgumentMatchers.any(PnFunctionality.class),
				ArgumentMatchers.any(OffsetDateTime.class))).thenReturn(optionalDowntimeLogs);
	}
	@SuppressWarnings("unchecked")
	protected void mockFindDowntimeBeforeOneYear() {
		Mockito.when(mockDowntimeLogsRepository.findOpenDowntimeLogsFuture(ArgumentMatchers.any(OffsetDateTime.class),
				ArgumentMatchers.any(PnFunctionality.class), ArgumentMatchers.any(OffsetDateTime.class)))
				.thenReturn(Optional.empty());

		Mockito.when(mockDowntimeLogsRepository.findDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists(
				ArgumentMatchers.any(OffsetDateTime.class), ArgumentMatchers.any(PnFunctionality.class),
				ArgumentMatchers.any(OffsetDateTime.class))).thenReturn(Optional.empty());

		Optional<DowntimeLogs> optionalDowntimeLogs = Optional
				.of(getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2021-09-28T08:55:15.995Z"),
						PnFunctionality.NOTIFICATION_CREATE, "EVENT", "akdocdfe-50403", null));

		Mockito.when(mockDowntimeLogsRepository.findLastDowntimeLogsWithoutEndDate(ArgumentMatchers.any(OffsetDateTime.class),
				ArgumentMatchers.any(PnFunctionality.class), ArgumentMatchers.any(OffsetDateTime.class)))
				.thenReturn(Optional.empty(), optionalDowntimeLogs);
	}

	protected void mockAddStatusChangeOKError() {
		Mockito.when(mockDowntimeLogsRepository.findOpenDowntimeLogsFuture(ArgumentMatchers.any(OffsetDateTime.class),
				ArgumentMatchers.any(PnFunctionality.class), ArgumentMatchers.any(OffsetDateTime.class)))
				.thenReturn(Optional.empty());

		Mockito.when(mockDowntimeLogsRepository.findDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists(
				ArgumentMatchers.any(OffsetDateTime.class), ArgumentMatchers.any(PnFunctionality.class),
				ArgumentMatchers.any(OffsetDateTime.class))).thenReturn(Optional.empty());

		Optional<DowntimeLogs> optionalDowntimeLogs = Optional.of(getDowntimeLogs("NOTIFICATION_CREATE2022",
				OffsetDateTime.parse("2022-09-28T08:55:15.995Z"), PnFunctionality.NOTIFICATION_CREATE, "EVENT",
				"akdocdfe-50403", OffsetDateTime.parse("2022-09-29T12:55:15.995Z")));

		Mockito.when(mockDowntimeLogsRepository.findLastDowntimeLogsWithoutEndDate(ArgumentMatchers.any(OffsetDateTime.class),
				ArgumentMatchers.any(PnFunctionality.class), ArgumentMatchers.any(OffsetDateTime.class)))
				.thenReturn(optionalDowntimeLogs);
	}

	protected void mockFoundAnyOpenDowntimeLogs() {
		Mockito.when(mockDowntimeLogsRepository.findOpenDowntimeLogsFuture(ArgumentMatchers.any(OffsetDateTime.class),
				ArgumentMatchers.any(PnFunctionality.class), ArgumentMatchers.any(OffsetDateTime.class)))
				.thenReturn(Optional.empty());

		Mockito.when(mockDowntimeLogsRepository.findDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists(
				ArgumentMatchers.any(OffsetDateTime.class), ArgumentMatchers.any(PnFunctionality.class),
				ArgumentMatchers.any(OffsetDateTime.class))).thenReturn(Optional.empty());
		
		Mockito.when(mockDowntimeLogsRepository.findLastDowntimeLogsWithoutEndDate(ArgumentMatchers.any(OffsetDateTime.class),
				ArgumentMatchers.any(PnFunctionality.class), ArgumentMatchers.any(OffsetDateTime.class)))
				.thenReturn(Optional.empty());
	}

	@SuppressWarnings("unchecked")
	protected void mockCurrentStatus500(RestTemplate client) throws IOException {
		String mock = getStringFromResourse(currentStatus500);
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class)))
				.thenReturn(response);
	}

	@SuppressWarnings("unchecked")
	protected void mockCurrentStatusOK(RestTemplate client) throws IOException {
		String mock = getStringFromResourse(currentStatus);
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class)))
				.thenReturn(response);
	}

	protected static LinkedMultiValueMap<String, String> getMockHistoryStatus(OffsetDateTime fromTime,
			OffsetDateTime toTime, List<PnFunctionality> functionality, String page, String size)
			throws JsonProcessingException {
		LinkedMultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
		String fromTimeString = fromTime != null ? fromTime.toString() : "";
		String toTimeString = toTime != null ? toTime.toString() : "";
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

	@SuppressWarnings("unchecked")
	protected void mockHistoryStatus(RestTemplate client) throws IOException {
		String mock = getStringFromResourse(historyStatus);
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class)))
				.thenReturn(response);
	}

	protected void mockHistory_BADREQUEST(RestTemplate client) {
		DowntimeLogsService serviceDowntime = Mockito.mock(DowntimeLogsService.class);

		Mockito.when(
				serviceDowntime.getStatusHistory(ArgumentMatchers.isNull(), ArgumentMatchers.any(OffsetDateTime.class),
						ArgumentMatchers.isNull(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
				.thenThrow(new RuntimeException("The starting date is required."));
	}

	protected void mockLegalFactId(RestTemplate client) {
		FileDownloadResponse response = new FileDownloadResponse();

		FileDownloadInfo downloadLegalFactDto = new FileDownloadInfo();
		downloadLegalFactDto.setUrl("http://localhost:9090");

		response.setVersionId("tQ74qWG0vAywePcNc");
		response.setDocumentType("PN_DOWNTIME_LEGAL_FACTS");
		response.setContentType("application/pdf");
		response.setContentLength(new BigDecimal(104697));
		response.setChecksum("cSSf87ZqNi9Dn8lZ1cDJUDNub");
		response.setKey("PN_DOWNTIME_LEGAL_FACTS-0002-L83U-NGPH-WHUF-I87S");

		response.setDownload(downloadLegalFactDto);
		response.setDocumentStatus("PRELOADED");
		response.setRetentionUntil(OffsetDateTime.parse("2033-07-27T00:00:00.000Z"));

		Mockito.when(fileDownloadApi.getFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
				.thenReturn(response);
	}

	protected void mockLegalFactIdError(RestTemplate client) {
		Mockito.when(fileDownloadApi.getFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
				.thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED,
						"403 Forbidden: [{\"resultDescription\": \"Unauthorized\", \"errorList\": [\"client is not allowed to read doc type PROVA\"], \"resultCode\": \"403.00\"}]"));
	}

	@SuppressWarnings("unchecked")
	protected void mockAddStatusChange_KO(RestTemplate client) {
		String mock = "";
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(response);
	}

	protected void mockAddStatusChange_OK(RestTemplate client) {
		FileCreationResponse fileCreationResponse = new FileCreationResponse();
		fileCreationResponse.setUploadMethod(UploadMethodEnum.PUT);
		fileCreationResponse.setKey("PN_DOWNTIME_LEGAL_FACTS-0002-L83U-NGPH-WHUF-I87S");
		fileCreationResponse.setSecret("123930");
		fileCreationResponse.setUploadUrl("http://amazon_url");
		ResponseEntity<FileCreationResponse> responseUpload = new ResponseEntity<>(fileCreationResponse, HttpStatus.OK);
		Mockito.when(fileUploadApi.createFileWithHttpInfo(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.any())).thenReturn(responseUpload);
		ResponseEntity<Object> response = new ResponseEntity<>(HttpStatus.OK);
		Mockito.when(client.exchange(ArgumentMatchers.any(URI.class), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<Object>>any())).thenReturn(response);
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

	private static String getStringFromResourse(Resource resource) throws IOException {
		return StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
	}

	protected String getMessageCloudwatchFromResource() throws JsonParseException, JsonMappingException, IOException {
		return StreamUtils.copyToString(mockMessageCloudwatch.getInputStream(), Charset.defaultCharset());
	}

	protected String getMessageLegalFactIdFromResource() throws JsonParseException, JsonMappingException, IOException {
		return StreamUtils.copyToString(mockMessageLegalFactId.getInputStream(), Charset.defaultCharset());
	}

	protected String getMessageActsQueueFromResource() throws JsonParseException, JsonMappingException, IOException {
		return StreamUtils.copyToString(mockMessageActsQueue.getInputStream(), Charset.defaultCharset());
	}

	protected void before() {
		service = new DowntimeLogsServiceImpl();
	}
}
