package it.pagopa.pn.downtime.middleware.legalfactgenerator;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.PnDowntimeApplication;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.impl.LegalFactGeneratorDocComposition;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.util.DocumentComposition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class LegalFactGeneratorDocCompositionIntTest {
    @Autowired
    private DocumentComposition documentComposition;

    @Test
    void testGenerateMalfunctionLegalFactPdf() throws IOException, TemplateException {
        // Arrange
        LegalFactGeneratorDocComposition legalFactGenerator = new LegalFactGeneratorDocComposition(documentComposition);
        DowntimeLogs downtime = sampleDowntimeLogs();

        // Act
        byte[] pdfBytes = legalFactGenerator.generateMalfunctionLegalFact(downtime);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        String outputPath = "target/test_LegalFactMalfunction.pdf";
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(pdfBytes);
        }

        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            assertNotNull(document);
            assertTrue(document.getPages().getCount() > 0, "PDF should contain at least one page");
        } catch (IOException e) {
            fail("Generated PDF is not valid");
        }

        System.out.println("PDF generated and saved to " + outputPath);
    }

    private DowntimeLogs sampleDowntimeLogs() {
        DowntimeLogs downtime = new DowntimeLogs();
        downtime.setFunctionalityStartYear("2023");
        downtime.setStartDate(OffsetDateTime.of(2023, 6, 1, 12, 0, 0, 0, ZoneOffset.UTC));
        downtime.setEndDate(OffsetDateTime.of(2023, 6, 1, 14, 0, 0, 0, ZoneOffset.UTC));
        downtime.setLegalFactId("legal-fact-id");
        downtime.setFileAvailable(true);
        downtime.setStartDateAttribute(OffsetDateTime.now(ZoneOffset.UTC));
        return downtime;
    }
}