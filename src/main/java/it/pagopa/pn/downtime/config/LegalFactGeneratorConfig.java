package it.pagopa.pn.downtime.config;

import it.pagopa.pn.downtime.middleware.externalclient.TemplatesClient;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.LegalFactGenerator;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.impl.LegalFactGeneratorDocComposition;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.impl.LegalFactGeneratorTemplates;
import it.pagopa.pn.downtime.util.DocumentComposition;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Classe di configurazione per impostare i bean di Template Generator in base alle proprietà dell'applicazione.
 * <p>
 * Questa configurazione determina quale implementazione dell'interfaccia {@link LegalFactGenerator}
 * deve essere istanziata a seconda del valore della proprietà
 * {@code pn.user-attributes.enable-templates-engine}.
 * </p>
 *
 * <p>
 * Quando la proprietà è impostata su {@code true} (o non è definita), viene utilizzata
 * l'implementazione {@link LegalFactGeneratorTemplates}
 * Quando la proprietà è impostata su {@code false}, viene utilizzata l'implementazione
 * {@link LegalFactGeneratorDocComposition}.
 * </p>
 *
 * <p>
 * **Nota:** Quando l'implementazione {@link LegalFactGeneratorDocComposition} non sarà più necessaria,
 * si dovrà eliminare la classe presente  {@code LegalFactGeneratorConfig}, {@link LegalFactGeneratorDocComposition} e
 * la properties pn.delivery-push.enable-templates-engine.
 * </p>
 */
@Configuration
@AllArgsConstructor
public class LegalFactGeneratorConfig {

    private final TemplatesClient templatesClient;
    private final DocumentComposition documentComposition;

    @Bean
    @ConditionalOnProperty(name = "pn.downtime-logs.enable-templates-engine", havingValue = "true", matchIfMissing = true)
    public LegalFactGenerator legalFactGeneratorTemplates() {
        return new LegalFactGeneratorTemplates(templatesClient);
    }

    @Bean
    @ConditionalOnProperty(name = "pn.downtime-logs.enable-templates-engine", havingValue = "false")
    public LegalFactGenerator legalFactGeneratorDocComposition() {
        return new LegalFactGeneratorDocComposition(documentComposition);
    }
}