package it.pagopa.pn.downtime;

import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.service.DowntimeLogsServiceImpl;

public abstract class AbstractMock {

	@Autowired
	MockMvc mvc;
	@MockBean
	RestTemplate client;

	@InjectMocks
	protected DowntimeLogsServiceImpl service;

	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
	private static ObjectMapper mapper = new ObjectMapper();

	protected final String currentStatusUrl = "/downtime/status";
	protected final String historyStatusUrl = "/downtime/history";
	protected final String eventsUrl = "/downtime/events";
	protected final String legalFactIdUrl = "/downtime/legal-facts/{legalFactId}";

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
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class)))
				.thenReturn(response);
	}

	@SuppressWarnings("unchecked")
	protected void mockAddStatusChange(RestTemplate client) {
		String mock = "";
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(response);
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

}
