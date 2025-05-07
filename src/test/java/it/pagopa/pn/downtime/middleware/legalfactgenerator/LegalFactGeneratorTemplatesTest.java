package it.pagopa.pn.downtime.middleware.legalfactgenerator;

import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.model.LanguageEnum;
import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.model.MalfunctionLegalFact;
import it.pagopa.pn.downtime.middleware.externalclient.TemplatesClient;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.impl.LegalFactGeneratorTemplates;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class LegalFactGeneratorTemplatesTest {
    @Mock
    private TemplatesClient templatesClient;

    @InjectMocks
    private LegalFactGeneratorTemplates legalFactGeneratorTemplates;


    @Test
    void testGenerateMalfunctionLegalFact_Success() {
        // Arrange
        LanguageEnum language = LanguageEnum.IT;
        DowntimeLogs downtimeLogs = sampleDowntimeLogs();
        byte[] expectedPdfBytes = "Test PDF Content".getBytes();

        when(templatesClient.malfunctionLegalFact(eq(language), any(MalfunctionLegalFact.class)))
                .thenReturn(expectedPdfBytes);

        // Act
        byte[] result = legalFactGeneratorTemplates.generateMalfunctionLegalFact(downtimeLogs);

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedPdfBytes, result);

        ArgumentCaptor<MalfunctionLegalFact> captor = ArgumentCaptor.forClass(MalfunctionLegalFact.class);
        verify(templatesClient).malfunctionLegalFact(eq(language), captor.capture());

        MalfunctionLegalFact captured = captor.getValue();
        assertEquals("01/06/2024", captured.getStartDate());
        assertEquals("14:00", captured.getTimeReferenceStartDate());
        assertEquals("01/06/2024", captured.getEndDate());
        assertEquals("16:00", captured.getTimeReferenceEndDate());
    }

    @Test
    void testGenerateMalfunctionLegalFact_ClientException() {
        // Arrange
        DowntimeLogs downtimeLogs = sampleDowntimeLogs();

        when(templatesClient.malfunctionLegalFact(eq(LanguageEnum.IT), any(MalfunctionLegalFact.class)))
                .thenThrow(new RuntimeException("Client Error"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                legalFactGeneratorTemplates.generateMalfunctionLegalFact(downtimeLogs)
        );
        assertEquals("Client Error", thrown.getMessage());
    }

    @Test
    void testGenerateMalfunctionLegalFact_EmptyFields() {
        // Arrange
        LanguageEnum language = LanguageEnum.IT;
        DowntimeLogs downtimeLogs = new DowntimeLogs();
        byte[] expectedPdfBytes = "Test PDF Content".getBytes();

        when(templatesClient.malfunctionLegalFact(eq(language), any(MalfunctionLegalFact.class)))
                .thenReturn(expectedPdfBytes);

        // Act
        byte[] result = legalFactGeneratorTemplates.generateMalfunctionLegalFact(downtimeLogs);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<MalfunctionLegalFact> captor = ArgumentCaptor.forClass(MalfunctionLegalFact.class);
        verify(templatesClient).malfunctionLegalFact(eq(language), captor.capture());

        MalfunctionLegalFact captured = captor.getValue();
        assertEquals("", captured.getStartDate());
        assertEquals("", captured.getTimeReferenceStartDate());
        assertEquals("", captured.getEndDate());
        assertEquals("", captured.getTimeReferenceEndDate());
    }

    @Test
    void testGenerateMalfunctionLegalFact_ByMalfunctionLegalFact() {
        // Arrange
        LanguageEnum language = LanguageEnum.IT;
        MalfunctionLegalFact malfunctionLegalFact = new MalfunctionLegalFact();
        malfunctionLegalFact.setStartDate("01/06/2024");
        malfunctionLegalFact.setTimeReferenceStartDate("14:00");
        malfunctionLegalFact.setEndDate("01/06/2024");
        malfunctionLegalFact.setTimeReferenceEndDate("16:00");
        malfunctionLegalFact.setHtmlDescription("<p>Sample <b>Test Description</b></p>");

        byte[] expectedPdfBytes = "Test PDF Content".getBytes();

        when(templatesClient.malfunctionLegalFact(eq(language), any(MalfunctionLegalFact.class)))
                .thenReturn(expectedPdfBytes);

        // Act
        byte[] result = legalFactGeneratorTemplates.generateMalfunctionLegalFact(malfunctionLegalFact);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<MalfunctionLegalFact> captor = ArgumentCaptor.forClass(MalfunctionLegalFact.class);
        verify(templatesClient).malfunctionLegalFact(eq(language), captor.capture());

        MalfunctionLegalFact captured = captor.getValue();
        assertEquals("01/06/2024", captured.getStartDate());
        assertEquals("14:00", captured.getTimeReferenceStartDate());
        assertEquals("01/06/2024", captured.getEndDate());
        assertEquals("16:00", captured.getTimeReferenceEndDate());
        assertEquals("<p>Sample <b>Test Description</b></p>", captured.getHtmlDescription());
    }

    private DowntimeLogs sampleDowntimeLogs() {
        DowntimeLogs downtime = new DowntimeLogs();
        downtime.setStartDate(OffsetDateTime.of(2024, 6, 1, 12, 0, 0, 0, ZoneOffset.UTC));
        downtime.setEndDate(OffsetDateTime.of(2024, 6, 1, 14, 0, 0, 0, ZoneOffset.UTC));
        downtime.setLegalFactId("legal-fact-id");
        downtime.setFileAvailable(true);
        downtime.setStartDateAttribute(OffsetDateTime.now(ZoneOffset.UTC));
        return downtime;
    }
}