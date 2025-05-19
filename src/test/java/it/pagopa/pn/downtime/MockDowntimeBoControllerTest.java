package it.pagopa.pn.downtime;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionalityStatus;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.service.impl.EventServiceImpl;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"pn.downtime-logs.enable-templates-engine=true"})
public class MockDowntimeBoControllerTest extends AbstractMock {

    @MockBean
    private EventServiceImpl eventService;

    @Test
    public void getMalfunctionPreview() throws Exception {
        mockResolved(client);

        byte[] mockPdf = "fake-pdf".getBytes();

        Mockito.when(eventService.previewLegalFact(Mockito.any(PnStatusUpdateEvent.class)))
                .thenReturn(mockPdf);

        String event = getBoStatusUpdateEvent(OffsetDateTime.parse("2022-08-28T15:55:15.995Z"),
                PnFunctionality.NOTIFICATION_CREATE, PnFunctionalityStatus.OK, "Html description");

        MockHttpServletResponse response = mvc
                .perform(
                        put("/downtime-bo/v1/legal-facts/malfunction/preview")
                                .content(event)
                                .header("x-pagopa-pn-uid", "PAGO-PA-OK")
                                .contentType(APPLICATION_JSON_UTF8)
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentType()).isEqualTo("application/pdf");
    }

    @Test
    public void addStatusChangeEventBo_Error() throws Exception {
        String event = getBoStatusUpdateEvent(OffsetDateTime.parse("2022-08-28T15:55:15.995Z"),
                PnFunctionality.NOTIFICATION_CREATE, PnFunctionalityStatus.OK, null);

        MockHttpServletResponse response = mvc
                .perform(
                        post("/downtime-bo/v1/events")
                                .content(event)
                                .header("x-pagopa-pn-uid", "PAGO-PA-OK")
                                .contentType(APPLICATION_JSON_UTF8)
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void addStatusChangeEventBo() throws Exception {
        String event = getBoStatusUpdateEvent(OffsetDateTime.parse("2022-08-28T15:55:15.995Z"),
                PnFunctionality.NOTIFICATION_CREATE, PnFunctionalityStatus.OK, "some malfunction description");

        Mockito.doNothing().when(eventService).addStatusChangeEvent(Mockito.anyString(), Mockito.anyList());

        MockHttpServletResponse response = mvc
                .perform(
                        post("/downtime-bo/v1/events")
                                .content(event)
                                .header("x-pagopa-pn-uid", "PAGO-PA-OK")
                                .contentType(APPLICATION_JSON_UTF8)
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
}
