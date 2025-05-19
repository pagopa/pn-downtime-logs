package it.pagopa.pn.downtime.service.impl;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionalityStatus;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.LegalFactGenerator;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Spy
    @InjectMocks
    EventServiceImpl eventService;

    @Mock
    private LegalFactGenerator legalFactGenerator;

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
    }

    @Test
    void previewLegalFactError() {
        PnStatusUpdateEvent event = getMockEvent();
        Mockito.doReturn(null).when(eventService).findDowntimeLogs(event.getTimestamp(),
                event.getFunctionality().get(0), event);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> eventService.previewLegalFact(event));

        assertEquals(Constants.GENERIC_CONFLICT_ERROR_MESSAGE_TITLE, exception.getMessage());
    }


    private PnStatusUpdateEvent getMockEvent() {
        PnStatusUpdateEvent mockEvent = new PnStatusUpdateEvent();
        mockEvent.setHtmlDescription("html");
        mockEvent.setSourceType(PnStatusUpdateEvent.SourceTypeEnum.OPERATOR);
        mockEvent.setSource("source");
        List<PnFunctionality> functionalities = new ArrayList<>();
        functionalities.add(PnFunctionality.NOTIFICATION_WORKFLOW);
        mockEvent.setFunctionality(functionalities);
        mockEvent.setStatus(PnFunctionalityStatus.KO);
        mockEvent.setTimestamp(OffsetDateTime.parse("2022-08-28T15:55:15.995Z"));
        return mockEvent;
    }

}