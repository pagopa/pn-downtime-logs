package it.pagopa.pn.downtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import it.pagopa.pn.downtime.model.Alarm;
import it.pagopa.pn.downtime.model.Dimensions;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.model.Trigger;
import it.pagopa.pn.downtime.pn_downtime.api.DowntimeApi;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.service.LegalFactService;

@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)

public class MockDowntimeLogsController extends AbstractMock {

	DowntimeApi downtimeApi = spy(DowntimeApi.class);
	
	@Autowired
	LegalFactService legalFactService ;

	@Test
	public void test_AddStatusChangeEventInterface() throws Exception {
		mockAddStatusChange_KO(client);
		assertThat(
				downtimeApi.addStatusChangeEvent("PAGO-PA-OK", getAddStatusChangeEventInterface()).getStatusCodeValue())
				.isEqualTo(HttpStatus.NOT_IMPLEMENTED.value());
	}

	@Test
	public void test_CurrentStatusInterface() throws Exception {
		mockCurrentStatus(client);

		assertThat(downtimeApi.currentStatus().getStatusCodeValue()).isEqualTo(HttpStatus.NOT_IMPLEMENTED.value());
	}

	@Test
	public void test_LegalFactInterface() throws Exception {
		mockLegalFactId(client);
		assertThat(downtimeApi.getLegalFact("PN_LEGAL_FACTS-0002-19PR-C7IB-SGGZ-VOYY").getStatusCodeValue())
				.isEqualTo(HttpStatus.NOT_IMPLEMENTED.value());
	}

	@Test
	public void test_StatusHistoryInterface() throws Exception {
		mockHistoryStatus(client);
		List<PnFunctionality> functionality = new ArrayList<>();
		functionality.add(PnFunctionality.NOTIFICATION_CREATE);
		functionality.add(PnFunctionality.NOTIFICATION_WORKFLOW);
		assertThat(downtimeApi
				.statusHistory(OffsetDateTime.parse("2022-01-23T04:56:07.000+00:00"),
						OffsetDateTime.parse("2022-09-28T12:56:07.000+00:00"), functionality, "0", "5")
				.getStatusCodeValue()).isEqualTo(HttpStatus.NOT_IMPLEMENTED.value());
	}

	@Test
	public void test_CheckCurretStatus() throws Exception {
		mockCurrentStatus(client);
		mockFindByFunctionalityAndEndDateIsNull(
				getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_CREATE, "EVENT_START", "akdoe-50403",null));
		MockHttpServletResponse response = mvc.perform(get(currentStatusUrl)).andReturn().getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("functionalities");
		assertThat(response.getContentAsString()).contains("openIncidents");
	}

	@Test
	public void test_CheckHistoryStatus() throws Exception {
		mockHistoryStatus(client);
		mockFindAllByFunctionalityInAndStartDateBetween();
		mockFindAllByFunctionalityInAndEndDateBetweenAndStartDateBefore();
		List<PnFunctionality> functionality = new ArrayList<>();
		functionality.add(PnFunctionality.NOTIFICATION_CREATE);
		functionality.add(PnFunctionality.NOTIFICATION_WORKFLOW);
		functionality.add(PnFunctionality.NOTIFICATION_VISUALIZZATION);
		MockHttpServletResponse response = mvc
				.perform(get(historyStatusUrl)
						.params(getMockHistoryStatus(OffsetDateTime.parse("2022-01-23T04:56:07.000+00:00"),
								OffsetDateTime.parse("2022-10-28T12:56:07.000+00:00"), functionality, "0", "5")))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("result");
		assertThat(response.getContentAsString()).contains("functionality");
	}

	@Test
	public void test_CheckHistoryStatusPage5() throws Exception {
		mockHistoryStatus(client);
		mockFindAllByFunctionalityInAndStartDateBetween();
		mockFindAllByFunctionalityInAndEndDateBetweenAndStartDateBefore();
		List<PnFunctionality> functionality = new ArrayList<>();
		functionality.add(PnFunctionality.NOTIFICATION_CREATE);
		functionality.add(PnFunctionality.NOTIFICATION_WORKFLOW);
		functionality.add(PnFunctionality.NOTIFICATION_VISUALIZZATION);
		MockHttpServletResponse response = mvc
				.perform(get(historyStatusUrl)
						.params(getMockHistoryStatus(OffsetDateTime.parse("2022-01-23T04:56:07.000+00:00"),
								OffsetDateTime.parse("2022-09-28T12:56:07.000+00:00"), functionality, "5", "5")))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("result");
		assertThat(response.getContentAsString()).contains("nextPage");
	}
	
	@Test
	public void test_CheckHistoryErrorFunctionality() throws Exception {
		mockHistory_BADREQUEST(client);
		 mvc.perform(get(historyStatusUrl).params(getMockHistoryStatus(null,
				 OffsetDateTime.parse("2022-09-28T12:56:07.000+00:00"), null, "0", "5")))
		 .andExpect(status().isBadRequest());
		
	}
	@Test
	public void test_CheckLegalFactId() throws Exception {
		mockLegalFactId(client);

		MockHttpServletResponse response = mvc.perform(get(legalFactIdUrl.concat("PN_LEGAL_FACTS-0002-L83U-NGPH-WHUF-I87S")))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("url");
		assertThat(response.getContentAsString()).contains("contentLength");
	}

	@Test
	public void test_CheckLegalFactIdNotExists() throws Exception {
		mockLegalFactIdError(client);

		MockHttpServletResponse response = mvc.perform(get(legalFactIdUrl.concat("PN_LEGAL_FACTS-NOT-EXISTS"))).andReturn()
				.getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
	}

	@Test
	public void test_CheckAddStatusChangeKOWithEndDate() throws Exception {
		mockAddStatusChange_KO(client);
		mockFindByFunctionalityAndStartDateLessThanEqualWithEndDate();
		List<PnFunctionality> pnFunctionality = new ArrayList<>();
		pnFunctionality.add(PnFunctionality.NOTIFICATION_CREATE);
		pnFunctionality.add(PnFunctionality.NOTIFICATION_WORKFLOW);

		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2022-08-28T15:55:15.995Z"),
				pnFunctionality, PnFunctionalityStatus.KO, SourceTypeEnum.ALARM, "ALARM");
		MockHttpServletResponse response = mvc
				.perform(post(eventsUrl).accept(APPLICATION_JSON_UTF8).content(pnStatusUpdateEvent)
						.contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid", "PAGO-PA-OK"))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}
	
	@Test
	public void test_CheckAddStatusChangeKONoEndDate() throws Exception {
		mockAddStatusChange_KO(client);
		mockFindByFunctionalityAndStartDateLessThanEqualNoEndDate();
		List<PnFunctionality> pnFunctionality = new ArrayList<>();
		pnFunctionality.add(PnFunctionality.NOTIFICATION_CREATE);
		pnFunctionality.add(PnFunctionality.NOTIFICATION_WORKFLOW);

		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2022-08-28T15:55:15.995Z"),
				pnFunctionality, PnFunctionalityStatus.KO, SourceTypeEnum.ALARM, "ALARM");
		MockHttpServletResponse response = mvc
				.perform(post(eventsUrl).accept(APPLICATION_JSON_UTF8).content(pnStatusUpdateEvent)
						.contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid", "PAGO-PA-OK"))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}
	@Test
	public void test_CheckAddStatusChangeOK() throws Exception {
		mockFindByFunctionalityAndStartDateLessThanEqualNoEndDate();
		List<PnFunctionality> pnFunctionality = new ArrayList<>();
		pnFunctionality.add(PnFunctionality.NOTIFICATION_CREATE);

		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2022-08-28T16:55:15.995Z"),
				pnFunctionality, PnFunctionalityStatus.OK, SourceTypeEnum.OPERATOR, "OPERATOR");
		MockHttpServletResponse response = mvc
				.perform(post(eventsUrl).accept(APPLICATION_JSON_UTF8).content(pnStatusUpdateEvent)
						.contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid", "PAGO-PA-OK"))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}
	
	@Test
	public void test_GenerateLegalFact() throws Exception {
		mockAddStatusChange_OK(client);
		DowntimeLogs downtime= getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"),
				PnFunctionality.NOTIFICATION_CREATE, "EVENT", "akdocdfe-50403",OffsetDateTime.parse("2022-08-28T08:55:15.995Z"));
		assertThat(legalFactService.generateLegalFact(downtime).toString()).contains("legalFactId");
	}
	
	@Test
	public void test_cloudwatchMapper() {
		Alarm alarm = new Alarm();
		Trigger trigger = new Trigger();
		Dimensions dimension = new Dimensions();
		List<Dimensions> listDimensions = new ArrayList<>();
		alarm.setAlarmDescription("CloudWatch alarm for when DLQ has 1 or more messages.");
		alarm.setNewStateValue("OK");
		alarm.setStateChangeTime(OffsetDateTime.parse("2022-10-24T21:00:15.995Z"));
		dimension.setValue("NOTIFICATION_CREATE");
		listDimensions.add(dimension);
		trigger.setDimensions(listDimensions);
		alarm.setTrigger(trigger);
		PnStatusUpdateEvent pnStatusUpdateEvent = cloudwatchMapper.alarmToPnStatusUpdateEvent(alarm);
		
		assertEquals(pnStatusUpdateEvent.getFunctionality().size(), listDimensions.size());
		assertEquals(pnStatusUpdateEvent.getStatus().toString(), alarm.getNewStateValue());
		assertEquals(pnStatusUpdateEvent.getTimestamp(), alarm.getStateChangeTime());
		assertEquals(pnStatusUpdateEvent.getSource(), alarm.getAlarmDescription());
	}
}
