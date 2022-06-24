package it.pagopa.pn.downtime;

import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusResponse;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
import it.pagopa.pn.downtime.service.DowntimeLogsService;

public abstract class AbstractMock {

	@Autowired
	MockMvc mvc;
	@MockBean
	RestTemplate client;




	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
	
	protected final String currentStatusUrl = "/downtime/status";

	@SuppressWarnings("unchecked")
	protected void mockCurrentStatus(RestTemplate client) {
		String mock = "{\"functionalities\":[\"NOTIFICATION_CREATE\",\"NOTIFICATION_VISUALIZZATION\",\"NOTIFICATION_WORKFLOW\"],\"openIncidents\":[{\"functionality\": \"NOTIFICATION_CREATE\",\"status\": \"KO\",\"startDate\": \"2022-08-24T08:55:15.995Z\",\"endDate\": null,\"legalFactId\": null}]}";
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class)))
			.thenReturn(response);
	}


}
