package it.pagopa.pn.downtime;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentCompositionTest {
    public byte[] html2Pdf(String baseUri, String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, baseUri);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void testHtml2Pdf() throws IOException {

        // Read the HTML template
        Path htmlTemplatePath = Paths.get("src/main/resources/documents_composition_templates/PdfLegalFact.html");
        String html = Files.readString(htmlTemplatePath);
        String baseUri = htmlTemplatePath.getParent().toUri().toString();

        // Replace placeholders with actual values
        String startDate = "14/06/2024";
        String timeReferenceStartDate = "17:57";
        String endDate = "14/06/2024";
        String timeReferenceEndDate = "18:00";
        html = html.replace("${startDate}", startDate);
        html = html.replace("${timeReferenceStartDate}", timeReferenceStartDate);
        html = html.replace("${endDate}", endDate);
        html = html.replace("${timeReferenceEndDate}", timeReferenceEndDate);

        // Convert HTML to PDF
        byte[] result = html2Pdf(baseUri, html);

        // Validate the result
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Save PDF to file
        String outputPath = "target/generated-test-sources/test_LegalFact.pdf";
        Path outputDir = Paths.get("output");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(result);
        }

        // Verify the PDF is valid
        try (ByteArrayInputStream bis = new ByteArrayInputStream(result)) {
            org.apache.pdfbox.pdmodel.PDDocument.load(bis);
        } catch (IOException e) {
            fail("Generated PDF is not valid");
        }

        System.out.println("PDF generated and saved to " + outputPath);
    }
}
