package it.pagopa.pn.downtime.service.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionalityStatus;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.LegalFactGenerator;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.producer.DowntimeLogsSend;
import it.pagopa.pn.downtime.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Spy
    @InjectMocks
    EventServiceImpl eventService;

    @Mock
    private LegalFactGenerator legalFactGenerator;

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    @Mock
    private DowntimeLogsSend producer;

    @Test
    void previewLegalFact() throws Exception {
        byte[] expected = "fake-pdf".getBytes();

        PnStatusUpdateEvent event = getMockEvent();

        DowntimeLogs d = new DowntimeLogs();
        Mockito.doReturn(d).when(eventService).findDowntimeLogs(event.getTimestamp(), event.getFunctionality().get(0), event);

        Mockito.when(legalFactGenerator.generateMalfunctionLegalFact(d))
                .thenReturn(expected);

        byte[] result = eventService.previewLegalFact(event);
        assertEquals(expected, result, "The previewLegalFact method should return the expected result");

        verify(eventService).sanitizeHtmlDescription(event.getHtmlDescription());
    }

    @Test
    void previewLegalFactError() {
        try {
            PnStatusUpdateEvent event = getMockEvent();

            Mockito.doReturn(null).when(eventService).findDowntimeLogs(event.getTimestamp(), event.getFunctionality().get(0), event);

            eventService.previewLegalFact(event);

        } catch (Exception e) {
            Exception expected = new IllegalArgumentException(Constants.GENERIC_CONFLICT_ERROR_MESSAGE_TITLE);
            assertEquals(expected.getClass(), e.getClass());
            assertEquals(expected.getMessage(), e.getMessage());
        }
    }

    private PnStatusUpdateEvent getMockEvent() {
        PnStatusUpdateEvent mockEvent = new PnStatusUpdateEvent();
        mockEvent.setHtmlDescription("<p>html <b>description</b></p>");
        mockEvent.setSourceType(PnStatusUpdateEvent.SourceTypeEnum.OPERATOR);
        mockEvent.setSource("source");
        List<PnFunctionality> functionalities = new ArrayList<>();
        functionalities.add(PnFunctionality.NOTIFICATION_WORKFLOW);
        mockEvent.setFunctionality(functionalities);
        mockEvent.setStatus(PnFunctionalityStatus.KO);
        mockEvent.setTimestamp(OffsetDateTime.parse("2022-08-28T15:55:15.995Z"));
        return mockEvent;
    }

    @Test
    void sanitizeHtmlDescriptionNull() {
        assertNull(eventService.sanitizeHtmlDescription(null));
    }

    @Test
    void sanitizeHtmlDescription() {
        String mixedHtml = """
                    <p style="color: red; font-size: 18px;">Questo è un <b style="font-weight: bold;">test</b> con <i style="font-style: italic;">tag validi</i>.</p>
                    <ul style="list-style-type: square;">
                      <li style="color: green;">Elemento uno</li>
                      <li style="color: blue;">Elemento due</li>
                    </ul>
                    <img src="invalid.jpg" onerror="alert('XSS')">
                    <a href="javascript:alert('XSS')">Click me</a>
                    <div style="background-image: url('javascript:alert(1)')">Test</div>
                    <iframe src="http://evil.example.com" width="100%" height="500"></iframe>
                    <script>alert('XSS');</script>
                    <svg><script>alert('XSS')</script></svg>
                """;
        String expected = """
                    <p>Questo è un <b>test</b> con <i>tag validi</i>.</p>
                    <ul><li>Elemento uno</li><li>Elemento due</li></ul>
                   \s
                    Click me
                    Test
                   \s
                   \s
                   \s
                """;

        assertEquals(expected, eventService.sanitizeHtmlDescription(mixedHtml));
    }

    @Test
    void checkUpdateDowntime() throws IOException {
        String eventId = "eventId";

        PnStatusUpdateEvent event = new PnStatusUpdateEvent();
        event.setTimestamp(OffsetDateTime.parse("2022-08-28T15:55:15.995Z"));
        event.setStatus(PnFunctionalityStatus.OK);
        event.setHtmlDescription("<p>html</p>");

        DowntimeLogs dt = new DowntimeLogs();

        eventService.checkUpdateDowntime(eventId, event, dt);

        verify(eventService).sanitizeHtmlDescription(event.getHtmlDescription());
        verify(dynamoDBMapper).save(Mockito.any());
        verify(producer).sendMessage(Mockito.any(), Mockito.any());
    }
}