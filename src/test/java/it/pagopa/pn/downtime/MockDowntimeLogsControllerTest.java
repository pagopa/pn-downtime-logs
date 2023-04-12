package it.pagopa.pn.downtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.pagopa.pn.downtime.model.Alarm;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.producer.DowntimeLogsSend;
import it.pagopa.pn.downtime.service.LegalFactService;

@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)

public class MockDowntimeLogsControllerTest extends AbstractMock {

	@Autowired
	LegalFactService legalFactService;

	@MockBean
	protected DowntimeLogsSend producer;

	/** jUnit test for the /curretStatus service */

	@Test
	public void test_CheckCurretStatus() throws Exception {
		mockCurrentStatus500(client);
		mockFindByFunctionalityAndEndDateIsNull(
				getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_CREATE, "EVENT_START", "akdoe-50403", null));
		MockHttpServletResponse response = mvc.perform(get(currentStatusUrl)).andReturn().getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("functionalities");
		assertThat(response.getContentAsString()).contains("openIncidents");
	}
	
	@Test
	public void callCurrentStatusError() throws Exception {
		mockStatusError();
		MockHttpServletResponse response = mvc.perform(get(currentStatusUrl)).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("KO");
		assertThat(response.getContentAsString()).contains("500");
	}
	/** jUnit test for the /historyStatus service */

	public void test_CheckHistoryStatus(boolean toTime) throws Exception {
		mockHistoryStatus(client);
		List<PnFunctionality> functionality = List.of(PnFunctionality.NOTIFICATION_CREATE, PnFunctionality.NOTIFICATION_WORKFLOW, PnFunctionality.NOTIFICATION_VISUALIZATION);
		
		MockHttpServletResponse response = null;
		if (!toTime) {
			response = mvc
					.perform(get(historyStatusUrl)
							.params(getMockHistoryStatus(OffsetDateTime.parse("2022-01-23T04:56:07.000+00:00"),
									OffsetDateTime.parse("2022-09-28T12:56:07.000+00:00"), functionality, "0", "5")))
					.andReturn().getResponse();
		} else {
			response = mvc
					.perform(get(historyStatusUrl).params(getMockHistoryStatus(
							OffsetDateTime.parse("2022-01-23T04:56:07.000+00:00"), null, functionality, "0", "5")))
					.andReturn().getResponse();
		}
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("result");
		assertThat(response.getContentAsString()).contains("functionality");
	}
	
	/**
	 * Two downtimeLogs with startDate between fromDate and toDate
	 */
	@Test
	public void test_CheckHistoryStatusStartDateBetween() throws Exception {
		mockFindByFunctionalityInAndStartDateBetween();
		test_CheckHistoryStatus(false);
	}

	/**
	 * A downtimeLogs with startDate before fromDate and endDate between fromDate and toDate
	 */
	@Test
	public void test_CheckHistoryStatusPageEndDateBetween() throws Exception {
		mockFindAllByFunctionalityInAndEndDateBetweenAndStartDateBefore();
		test_CheckHistoryStatus(false);
	}

	/**
	 * Three downtimeLogs with startDate after fromDate and toTime equal to null
	 */
	@Test
	public void test_CheckHistoryNoToTimeStartDateAfter() throws Exception {
		mockFindAllByFunctionalityInAndStartDateAfter();
		test_CheckHistoryStatus(true);
	}

	/**
	 * toTime equal to null
	 */
	@Test
	public void test_CheckHistoryNoToTimeEndDateAfterAndStartDateBefore() throws Exception {
		mockFindAllByFunctionalityInAndEndDateAfterAndStartDateBefore();
		test_CheckHistoryStatus(true);
	}

	/**
	 * The required fromTime field is missing
	 */
	@Test
	public void test_CheckHistoryErrorFromTime() throws Exception {
		mockHistory_BADREQUEST(client);
		mvc.perform(get(historyStatusUrl).params(
				getMockHistoryStatus(null, OffsetDateTime.parse("2022-09-28T12:56:07.000+00:00"), null, "0", "5")))
				.andExpect(status().isBadRequest());
	}

	/**
	 * The functionality array is empty
	 */
	@Test
	public void test_CheckHistoryFunctionalityIsEmpty() throws Exception {
		mockHistoryStatus(client);
		List<PnFunctionality> functionality = new ArrayList<>();

		MockHttpServletResponse response = mvc
				.perform(get(historyStatusUrl)
						.params(getMockHistoryStatus(OffsetDateTime.parse("2022-01-23T04:56:07.000+00:00"),
								OffsetDateTime.parse("2022-09-28T12:56:07.000+00:00"), functionality, "0", "5")))
				.andReturn().getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("result");
		assertThat(response.getContentAsString()).contains("nextPage");
	}

	/** jUnit test for the /legalFactId service */

	@Test
	public void test_CheckLegalFactIdIsNull() {
		List<DowntimeLogs> listDowntimeLogs = new ArrayList<>();
		listDowntimeLogs
				.add(getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_CREATE, "EVENT_START", "akdoe-50403", null));
		mockFindAllByEndDateIsNotNullAndLegalFactIdIsNullWithParallelScan(service, listDowntimeLogs);

		Assertions.assertNotNull(service.findAllByEndDateIsNotNullAndLegalFactIdIsNull());
	}

	@Test
	public void test_CheckLegalFactId() throws Exception {
		mockLegalFactId(client);
		MockHttpServletResponse response = mvc
				.perform(get(legalFactIdUrl.concat("PN_DOWNTIME_LEGAL_FACTS-0002-L83U-NGPH-WHUF-I87S"))).andReturn()
				.getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("url");
		assertThat(response.getContentAsString()).contains("contentLength");
	}

	@Test
	public void test_CheckLegalFactIdNotExists() throws Exception {
		mockLegalFactIdError(client);

		MockHttpServletResponse response = mvc.perform(get(legalFactIdUrl.concat("PN_DOWNTIME_LEGAL_FACTS-NOT-EXISTS")))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
	}

	@Test
	public void test_GenerateLegalFact() throws Exception {
		mockAddStatusChange_OK(client);
		DowntimeLogs downtime = getDowntimeLogs("NOTIFICATION_CREATE2022",
				OffsetDateTime.parse("2022-08-28T08:55:15.995Z"), PnFunctionality.NOTIFICATION_CREATE, "EVENT",
				"akdocdfe-50403", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"));
		assertThat(legalFactService.generateLegalFact(downtime).toString()).contains("legalFactId");
	}

	/** jUnit test for the /addStatusChange service */

	/** 
	 * A KO event arrives and there is no other open 
	 */
	@Test
	public void test_CheckAddStatusChangeKOWithEndDate() throws Exception {
		mockFoundAnyOpenDowntimeLogs();
		mockAddStatusChange_KO(client);
		mockSaveEvent();
		mockSaveDowntime();

		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2022-08-28T15:55:15.995Z"),
				List.of(PnFunctionality.NOTIFICATION_CREATE), PnFunctionalityStatus.KO, SourceTypeEnum.ALARM, "ALARM");
		
		MockHttpServletResponse response = mvc.perform(post(eventsUrl).content(pnStatusUpdateEvent)
				.contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid", "PAGO-PA-OK")).andReturn().getResponse();
		
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
	
	@Test
	public void test_CheckAddStatusChangeTimestampIsFuture() throws Exception {
		mockFoundAnyOpenDowntimeLogs();
		mockAddStatusChange_KO(client);
		
		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2099-08-28T08:55:15.995Z"),
				List.of(PnFunctionality.NOTIFICATION_WORKFLOW), PnFunctionalityStatus.KO, SourceTypeEnum.ALARM, "ALARM");
		MockHttpServletResponse response = mvc.perform(post(eventsUrl).content(pnStatusUpdateEvent)
				.contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid", "PAGO-PA-OK")).andReturn().getResponse();
		
		assertThat(response.getContentAsString()).contains("500");
	}
	
	@Test
	public void mockStatusChangeEventException() throws Exception {
		mockFindByFunctionalityAndStartDateLessThanEqualNoEndDate();
		doThrow(JsonProcessingException.class).when(producer).sendMessage(Mockito.any(DowntimeLogs.class), Mockito.anyString());

		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2022-08-28T15:55:15.995Z"),
				List.of(PnFunctionality.NOTIFICATION_CREATE), PnFunctionalityStatus.OK, SourceTypeEnum.ALARM, "ALARM");
		MockHttpServletResponse response = mvc.perform(post(eventsUrl).content(pnStatusUpdateEvent)
				.contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid", "PAGO-PA-OK")).andReturn().getResponse();
		
		assertThat(response.getContentAsString()).contains("500");
	}
	
	/** 
	 * A KO event arrives and there is other open 
	 */
	@Test
	public void test_CheckAddStatusChangeKONoEndDate() throws Exception {
		mockAddStatusChange_KO(client);
		mockFindByFunctionalityAndStartDateLessThanEqualNoEndDate();
		mockSaveEvent();
		
		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2022-08-28T15:55:15.995Z"),
				List.of(PnFunctionality.NOTIFICATION_CREATE), PnFunctionalityStatus.KO, SourceTypeEnum.ALARM, "ALARM");
		
		MockHttpServletResponse response = mvc.perform(post(eventsUrl).content(pnStatusUpdateEvent)
				.contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid", "PAGO-PA-OK")).andReturn().getResponse();
		
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}

	public void test_CheckAddStatusChange() throws Exception {
		mockAddStatusChange_OK(client);
		
		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2022-08-28T16:55:15.995Z"),
				List.of(PnFunctionality.NOTIFICATION_CREATE), PnFunctionalityStatus.OK, SourceTypeEnum.OPERATOR, "OPERATOR");
		MockHttpServletResponse response = mvc.perform(post(eventsUrl).content(pnStatusUpdateEvent)
				.contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid", "PAGO-PA-OK")).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}

	@Test
	public void test_CheckAddStatusChangeKO_GivenFutureDowntimeOpen() throws Exception {
		mockFindOpenDowntimeFuture();
		
		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2022-08-27T15:55:15.995Z"),
				List.of(PnFunctionality.NOTIFICATION_CREATE), PnFunctionalityStatus.KO, SourceTypeEnum.ALARM, "ALARM");
		
		MockHttpServletResponse response = mvc.perform(post(eventsUrl).content(pnStatusUpdateEvent)
				.contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid", "PAGO-PA-OK")).andReturn().getResponse();
		
		assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
	}
	
	@Test
	public void test_CheckAddStatusChangeKO_whenDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists() throws Exception {
		mockFindDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists();
		
		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2022-08-28T15:55:15.995Z"),
				List.of(PnFunctionality.NOTIFICATION_CREATE), PnFunctionalityStatus.KO, SourceTypeEnum.ALARM, "ALARM");
		
		MockHttpServletResponse response = mvc.perform(post(eventsUrl).content(pnStatusUpdateEvent)
				.contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid", "PAGO-PA-OK")).andReturn().getResponse();
		
		assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
	}
	/** 
	 * An OK event arrives and there is a KO event open 
	 */
	@Test
	public void test_CheckAddStatusChangeOK() throws Exception {
		mockFindByFunctionalityAndStartDateLessThanEqualNoEndDate();
		mockSaveEvent();
		mockSaveDowntime();
		mockProducer(producer);
		test_CheckAddStatusChange();
	}

	/** 
	 * An OK event arrives and there is a KO event open from the previous year 
	 */
	@Test
	public void test_CheckAddStatusChangeOKAfterYear() throws Exception {
		mockFindDowntimeBeforeOneYear();
		mockSaveEvent();
		mockSaveDowntime();
		mockProducer(producer);
		test_CheckAddStatusChange();
	}

	/** 
	 * An OK event arrives and there is a KO event already closed 
	 */
	@Test
	public void test_CheckAddStatusChangeOKError() throws Exception {
		mockAddStatusChange_OK(client);
		mockAddStatusChangeOKError();
		
		String pnStatusUpdateEvent = getPnStatusUpdateEvent(OffsetDateTime.parse("2023-04-05T15:06:47.327907Z"),
				 List.of(PnFunctionality.NOTIFICATION_CREATE), PnFunctionalityStatus.OK, SourceTypeEnum.OPERATOR, "OPERATOR");
		
		MockHttpServletResponse response = mvc.perform(post(eventsUrl).content(pnStatusUpdateEvent)
				.contentType(APPLICATION_JSON_UTF8).header("x-pagopa-pn-uid", "PAGO-PA-OK")).andReturn().getResponse();
		
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
	
	@Test
	public void test_CloudwatchMapper() {
		Alarm alarm = new Alarm();
		alarm.setAlarmName("queueName-NOTIFICATION_CREATE");
		alarm.setAlarmDescription("CloudWatch alarm for when DLQ has 1 or more messages.");
		alarm.setNewStateValue("OK");
		alarm.setStateChangeTime(OffsetDateTime.parse("2022-10-24T21:00:15.995Z"));
		PnStatusUpdateEvent pnStatusUpdateEvent = cloudwatchMapper.alarmToPnStatusUpdateEvent(alarm);

		assertEquals(pnStatusUpdateEvent.getStatus().toString(), alarm.getNewStateValue());
		assertEquals(pnStatusUpdateEvent.getTimestamp(), alarm.getStateChangeTime());
		assertEquals(pnStatusUpdateEvent.getSource(), alarm.getAlarmDescription());
	}

	/** jUnit test for the /status service */

	@Test
	public void test_CheckStatus500() throws Exception {
		mockCurrentStatus500(client);
		mockFindByFunctionalityAndEndDateIsNullCheck500(
				getDowntimeLogs("NOTIFICATION_CREATE2022", OffsetDateTime.parse("2022-08-28T13:55:15.995Z"),
						PnFunctionality.NOTIFICATION_CREATE, "EVENT_START", "akdoe-50403", null));
        MockHttpServletResponse response = mvc.perform(get(statusUrl)).andReturn().getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
		assertThat(response.getContentAsString()).contains("functionalities");
		assertThat(response.getContentAsString()).contains("openIncidents");
	}

	@Test
	public void test_CheckStatusOK() throws Exception {
		mockCurrentStatusOK(client);
		DowntimeLogs dt = new DowntimeLogs();
		mockFindByFunctionalityAndEndDateIsNull(dt);
        MockHttpServletResponse response = mvc.perform(get(statusUrl)).andReturn().getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("functionalities");
		assertThat(response.getContentAsString()).contains("openIncidents");
	}

}
