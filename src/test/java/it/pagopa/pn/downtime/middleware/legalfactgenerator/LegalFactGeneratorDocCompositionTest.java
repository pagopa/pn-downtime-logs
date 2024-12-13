package it.pagopa.pn.downtime.middleware.legalfactgenerator;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.impl.LegalFactGeneratorDocComposition;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.util.DocumentComposition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LegalFactGeneratorDocCompositionTest {
    @Mock
    private DocumentComposition documentComposition;

    @InjectMocks
    private LegalFactGeneratorDocComposition legalFactGenerator;


    @Test
    void testGenerateMalfunctionLegalFact_Success() throws IOException, TemplateException {
        // Arrange
        DowntimeLogs downtime = sampleDowntimeLogs();
        byte[] expectedPdfBytes = "Test PDF Content".getBytes();

        when(documentComposition.executePdfTemplate(
                eq(DocumentComposition.TemplateType.LEGAL_FACT),
                any(Map.class))
        ).thenReturn(expectedPdfBytes);

        // Act
        byte[] result = legalFactGenerator.generateMalfunctionLegalFact(downtime);

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedPdfBytes, result);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(documentComposition).executePdfTemplate(eq(DocumentComposition.TemplateType.LEGAL_FACT), captor.capture());

        Map<String, Object> capturedModel = captor.getValue();
        assertEquals("01/06/2024", capturedModel.get("startDate"));
        assertEquals("14:00", capturedModel.get("timeReferenceStartDate"));
        assertEquals("01/06/2024", capturedModel.get("endDate"));
        assertEquals("16:00", capturedModel.get("timeReferenceEndDate"));
    }

    @Test
    void testGenerateMalfunctionLegalFact_TemplateException() throws IOException, TemplateException {
        // Arrange
        DowntimeLogs downtime = sampleDowntimeLogs();
        when(documentComposition.executePdfTemplate(
                eq(DocumentComposition.TemplateType.LEGAL_FACT),
                any())
        ).thenThrow(new TemplateException("Template Error", null));

        // Act & Assert
        TemplateException exception = assertThrows(TemplateException.class,
                () -> legalFactGenerator.generateMalfunctionLegalFact(downtime));
        assertEquals("Template Error", exception.getMessage());
    }

    @Test
    void testGenerateMalfunctionLegalFact_EmptyResult() throws IOException, TemplateException {
        // Arrange
        DowntimeLogs downtime = sampleDowntimeLogs();
        when(documentComposition.executePdfTemplate(
                eq(DocumentComposition.TemplateType.LEGAL_FACT),
                any(Map.class))
        ).thenReturn(new byte[0]);

        // Act
        byte[] result = legalFactGenerator.generateMalfunctionLegalFact(downtime);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    private DowntimeLogs sampleDowntimeLogs() {
        DowntimeLogs downtime = new DowntimeLogs();
        downtime.setFunctionalityStartYear("2024");
        downtime.setStartDate(OffsetDateTime.of(2024, 6, 1, 12, 0, 0, 0, ZoneOffset.UTC));
        downtime.setEndDate(OffsetDateTime.of(2024, 6, 1, 14, 0, 0, 0, ZoneOffset.UTC));
        downtime.setLegalFactId("legal-fact-id");
        downtime.setFileAvailable(true);
        downtime.setStartDateAttribute(OffsetDateTime.now(ZoneOffset.UTC));
        return downtime;
    }
}