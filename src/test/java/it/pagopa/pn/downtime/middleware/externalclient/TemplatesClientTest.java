package it.pagopa.pn.downtime.middleware.externalclient;

import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.api.TemplateApi;
import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.model.LanguageEnum;
import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.model.MalfunctionLegalFact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TemplatesClientTest {
    @Mock
    private TemplateApi templateEngineClient;

    @InjectMocks
    private TemplatesClient templatesClient;


    @Test
    void testMalfunctionLegalFact_Success() {
        // Arrange
        LanguageEnum language = LanguageEnum.IT;
        MalfunctionLegalFact malfunctionLegalFact = new MalfunctionLegalFact();
        malfunctionLegalFact.setStartDate("01/06/2024");
        malfunctionLegalFact.setTimeReferenceStartDate("12:00");
        malfunctionLegalFact.setEndDate("01/06/2024");
        malfunctionLegalFact.setTimeReferenceEndDate("14:00");

        byte[] expectedPdfBytes = "Test PDF Content".getBytes();
        when(templateEngineClient.malfunctionLegalFact(language, malfunctionLegalFact))
                .thenReturn(expectedPdfBytes);

        // Act
        byte[] result = templatesClient.malfunctionLegalFact(language, malfunctionLegalFact);

        // Assert
        assertNotNull(result, "Result must not be null");
        assertArrayEquals(expectedPdfBytes, result, "Result match the expected PDF bytes");

        verify(templateEngineClient, times(1)).malfunctionLegalFact(language, malfunctionLegalFact);
    }

    @Test
    void testMalfunctionLegalFact_NullResponse() {
        // Arrange
        LanguageEnum language = LanguageEnum.IT;
        MalfunctionLegalFact malfunctionLegalFact = new MalfunctionLegalFact();
        malfunctionLegalFact.setStartDate("01/06/2024");
        malfunctionLegalFact.setTimeReferenceStartDate("12:00");
        malfunctionLegalFact.setEndDate("01/06/2024");
        malfunctionLegalFact.setTimeReferenceEndDate("14:00");

        when(templateEngineClient.malfunctionLegalFact(language, malfunctionLegalFact)).thenReturn(null);

        // Act
        byte[] result = templatesClient.malfunctionLegalFact(language, malfunctionLegalFact);

        // Assert
        assertNull(result, "Result null when API returns null");

        verify(templateEngineClient, times(1)).malfunctionLegalFact(language, malfunctionLegalFact);
    }

    @Test
    void testMalfunctionLegalFact_ExceptionThrown() {
        // Arrange
        LanguageEnum language = LanguageEnum.IT;
        MalfunctionLegalFact malfunctionLegalFact = new MalfunctionLegalFact();
        malfunctionLegalFact.setStartDate("01/06/2024");
        malfunctionLegalFact.setTimeReferenceStartDate("12:00");
        malfunctionLegalFact.setEndDate("01/06/2024");
        malfunctionLegalFact.setTimeReferenceEndDate("14:00");

        when(templateEngineClient.malfunctionLegalFact(language, malfunctionLegalFact))
                .thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () ->
                templatesClient.malfunctionLegalFact(language, malfunctionLegalFact));
        assertEquals("API Error", exception.getMessage(), "Exception message is matched");

        verify(templateEngineClient, times(1)).malfunctionLegalFact(language, malfunctionLegalFact);
    }
}