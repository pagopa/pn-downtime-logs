package it.pagopa.pn.downtime;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.util.DocumentComposition;
import it.pagopa.pn.downtime.util.LegalFactGenerator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class LegalFactGeneratorTest {

    @Test
    public void generateDowntimeLogsLegalFactPdf() throws IOException, TemplateException {
        Configuration freemarker = new Configuration(new Version(2, 3, 0));
        DocumentComposition documentComposition = new DocumentComposition(freemarker);
        LegalFactGenerator legalFactGenerator = new LegalFactGenerator(documentComposition);

        // Read the HTML template
        Path htmlTemplatePath = Paths.get("src/main/resources/documents_composition_templates/PdfLegalFact.html");

        //Generate downtime
        DowntimeLogs downtime = new DowntimeLogs();
        downtime.setFunctionalityStartYear("2023");
        downtime.setStartDate(OffsetDateTime.of(2023, 6, 1, 12, 0, 0, 0, ZoneOffset.UTC));
        downtime.setEndDate(OffsetDateTime.of(2023, 6, 1, 14, 0, 0, 0, ZoneOffset.UTC));
        downtime.setLegalFactId("legal-fact-id");
        downtime.setFileAvailable(true);
        downtime.setStartDateAttribute(OffsetDateTime.now(ZoneOffset.UTC));

        // Convert HTML to PDF
        byte[] result = legalFactGenerator.generateLegalFact(downtime);

        // Validate the result
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Save PDF to file
        String outputPath = "target/generated-test-sources/test_LegalFact.pdf";
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(result);
        }

        // Verify the PDF is valid
        try (ByteArrayInputStream bis = new ByteArrayInputStream(result)) {
            PDDocument.load(bis);
        } catch (IOException e) {
            fail("Generated PDF is not valid");
        }

        System.out.println("PDF generated and saved to " + outputPath);
    }
}
