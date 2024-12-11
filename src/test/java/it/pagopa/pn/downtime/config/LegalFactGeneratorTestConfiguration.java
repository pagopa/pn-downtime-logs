package it.pagopa.pn.downtime.config;

import it.pagopa.pn.downtime.middleware.externalclient.TemplatesClient;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.LegalFactGenerator;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.impl.LegalFactGeneratorDocComposition;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.impl.LegalFactGeneratorTemplates;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import it.pagopa.pn.downtime.util.*;


@TestConfiguration
public class LegalFactGeneratorTestConfiguration {

    @Bean
    @ConditionalOnProperty(name = "pn.downtime-logs.enableTemplatesEngine", havingValue = "true", matchIfMissing = true)
    public LegalFactGenerator legalFactGeneratorTemplates() {
        TemplatesClient mockTemplatesClient = Mockito.mock(TemplatesClient.class);
        Mockito.when(mockTemplatesClient.malfunctionLegalFact(Mockito.any(), Mockito.any()))
                .thenReturn("mocked-legal-fact".getBytes());
        return new LegalFactGeneratorTemplates(mockTemplatesClient);
    }

    @Bean
    @ConditionalOnProperty(name = "pn.downtime-logs.enableTemplatesEngine", havingValue = "false")
    public LegalFactGenerator legalFactGeneratorDocComposition(DocumentComposition documentComposition) {
        return new LegalFactGeneratorDocComposition(documentComposition);
    }
}