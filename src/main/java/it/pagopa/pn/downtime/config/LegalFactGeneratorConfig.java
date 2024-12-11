package it.pagopa.pn.downtime.config;

import it.pagopa.pn.downtime.middleware.legalfactgenerator.impl.LegalFactGeneratorDocComposition;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.impl.LegalFactGeneratorTemplates;
import it.pagopa.pn.downtime.middleware.externalclient.TemplatesClient;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.LegalFactGenerator;
import it.pagopa.pn.downtime.util.DocumentComposition;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@AllArgsConstructor
public class LegalFactGeneratorConfig {
    private final TemplatesClient templatesClient;
    private final DocumentComposition documentComposition;


    @Bean
    @ConditionalOnProperty(name = "pn.downtime-logs.enableTemplatesEngine", havingValue = "true", matchIfMissing = true)
    public LegalFactGenerator legalFactGeneratorTemplates() {
        return new LegalFactGeneratorTemplates(templatesClient);
    }

    @Bean
    @ConditionalOnProperty(name = "pn.downtime-logs.enableTemplatesEngine", havingValue = "false")
    public LegalFactGenerator legalFactGeneratorDocComposition() {
        return new LegalFactGeneratorDocComposition(documentComposition);
    }
}