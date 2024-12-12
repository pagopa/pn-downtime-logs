package it.pagopa.pn.downtime;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.service.LegalFactService;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class DowntimeLogsControllerByTemplateClientTest extends AbstractMock {

    @Autowired
    LegalFactService legalFactService;

    @Test
    public void test_GenerateLegalFact() throws Exception {
        mockAddStatusChange_OK(client);
        mockTemplatesClientBehavior();
        DowntimeLogs downtime = getDowntimeLogs("NOTIFICATION_CREATE2022",
                OffsetDateTime.parse("2022-08-28T08:55:15.995Z"), PnFunctionality.NOTIFICATION_CREATE, "EVENT",
                "akdocdfe-50403", OffsetDateTime.parse("2022-08-28T08:55:15.995Z"));
        assertThat(legalFactService.generateLegalFact(downtime).toString()).contains("legalFactId");
    }

}
