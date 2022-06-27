package it.pagopa.pn.downtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent.SourceTypeEnum;


@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc

public class MockDowntimeLogsController extends AbstractMock {
	
	@Test
	public void test_CheckCurretStatus() throws Exception {
		mockCurrentStatus(client);
		MockHttpServletResponse response = mvc.perform(get(currentStatusUrl))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("functionalities");
		assertThat(response.getContentAsString()).contains("openIncidents");
	}
	
	@Test
	public void test_CheckHistoryStatus() throws Exception {
		mockHistoryStatus(client);
		List<PnFunctionality> functionality = new ArrayList<>();
		functionality.add(PnFunctionality.NOTIFICATION_CREATE);
		functionality.add(PnFunctionality.NOTIFICATION_WORKFLOW);
		functionality.add(PnFunctionality.NOTIFICATION_VISUALIZZATION);
		MockHttpServletResponse response = mvc.perform(get(historyStatusUrl)
				.params(getMockHistoryStatus(OffsetDateTime.parse("2022-01-23T04:56:07.000+00:00"),
						OffsetDateTime.parse("2022-09-28T12:56:07.000+00:00"), functionality, "0", "5")))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("result");
		assertThat(response.getContentAsString()).contains("functionality");
	}
	
	@Test
	public void test_CheckHistoryStatusErrorFunctionality() throws Exception {
		mockHistoryStatus(client);
		List<PnFunctionality> functionality = new ArrayList<>();
		MockHttpServletResponse response = mvc.perform(get(historyStatusUrl)
				.params(getMockHistoryStatus(OffsetDateTime.parse("2022-01-23T04:56:07.000+00:00"),
						OffsetDateTime.parse("2022-09-28T12:56:07.000+00:00"), functionality, "0", "5")))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	public void test_CheckHistoryStatusErrorFromDate() throws Exception {
		mockHistoryStatus(client);
		List<PnFunctionality> functionality = new ArrayList<>();
		MockHttpServletResponse response = mvc.perform(get(historyStatusUrl)
				.params(getMockHistoryStatus(OffsetDateTime.parse("2022-10-23T04:56:07.000+00:00"),
						OffsetDateTime.parse("2022-09-28T12:56:07.000+00:00"), functionality, "0", "5")))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	public void test_CheckHistoryStatusPage5() throws Exception {
		mockHistoryStatus(client);
		List<PnFunctionality> functionality = new ArrayList<>();
		functionality.add(PnFunctionality.NOTIFICATION_CREATE);
		functionality.add(PnFunctionality.NOTIFICATION_WORKFLOW);
		functionality.add(PnFunctionality.NOTIFICATION_VISUALIZZATION);
		MockHttpServletResponse response = mvc.perform(get(historyStatusUrl)
				.params(getMockHistoryStatus(OffsetDateTime.parse("2022-01-23T04:56:07.000+00:00"),
						OffsetDateTime.parse("2022-09-28T12:56:07.000+00:00"), functionality, "5", "5")))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("result");
		assertThat(response.getContentAsString()).contains("nextPage");
	}
	
	@Test
	public void test_CheckLegalFactId() throws Exception {
		mockLegalFactId(client);
		
		MockHttpServletResponse response = mvc.perform(get(legalFactIdUrl, "PN_LEGAL_FACTS-0002-L83U-NGPH-WHUF-I87S"))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("url");
		assertThat(response.getContentAsString()).contains("contentLength");
	}
	
	@Test
	public void test_CheckLegalFactIdNotExists() throws Exception {
		mockLegalFactId(client);
		
		MockHttpServletResponse response = mvc.perform(get(legalFactIdUrl, "PN_LEGAL_FACTS-NOT-EXISTS"))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
	}
	
	@Test
	public void test_CheckAddStatusChangeKOEndDatePre() throws Exception {
		mockAddStatusChange(client);
		List<PnFunctionality> pnFunctionality = new ArrayList<>();
		pnFunctionality.add(PnFunctionality.NOTIFICATION_CREATE);
		
		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2022-08-24T09:55:15.995Z"), pnFunctionality,
						PnFunctionalityStatus.KO, SourceTypeEnum.ALARM, "ALARM");
		MockHttpServletResponse response = mvc.perform(post(eventsUrl).accept(APPLICATION_JSON_UTF8)
				.content(pnStatusUpdateEvent).contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid","PAGO-PA-OK"))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}
	
	@Test
	public void test_CheckAddStatusChangeKOEndDatePost() throws Exception {
		mockAddStatusChange(client);
		List<PnFunctionality> pnFunctionality = new ArrayList<>();
		pnFunctionality.add(PnFunctionality.NOTIFICATION_CREATE);
		pnFunctionality.add(PnFunctionality.NOTIFICATION_WORKFLOW);
		
		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2022-08-28T08:55:15.995Z"), pnFunctionality,
						PnFunctionalityStatus.KO, SourceTypeEnum.ALARM, "ALARM");
		MockHttpServletResponse response = mvc.perform(post(eventsUrl).accept(APPLICATION_JSON_UTF8)
				.content(pnStatusUpdateEvent).contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid","PAGO-PA-KO"))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}
	
	@Test
	public void test_CheckAddStatusChangeOK() throws Exception {
		mockAddStatusChange(client);
		List<PnFunctionality> pnFunctionality = new ArrayList<>();
		pnFunctionality.add(PnFunctionality.NOTIFICATION_CREATE);
		pnFunctionality.add(PnFunctionality.NOTIFICATION_WORKFLOW);
		
		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2022-08-28T09:55:15.995Z"), pnFunctionality,
						PnFunctionalityStatus.OK, SourceTypeEnum.OPERATOR, "OPERATOR");
		MockHttpServletResponse response = mvc.perform(post(eventsUrl).accept(APPLICATION_JSON_UTF8)
				.content(pnStatusUpdateEvent).contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid","PAGO-PA-OK"))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}
}
