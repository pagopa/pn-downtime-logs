package it.pagopa.pn.downtime.middleware.externalclient;

import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.api.TemplateApi;
import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.model.LanguageEnum;
import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.model.MalfunctionLegalFact;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@CustomLog
public class TemplatesClient {

    private final TemplateApi templateEngineClient;

    /**
     * Generates the pdf of the malfunction legal fact.
     * This method is invoked to retrieve the legal fact from PnTemplatesEngine microservice.
     *
     * @param language is the language chosen for the generation of the legal fact
     * @param malfunctionLegalFact is the dto that contains all document's data to be inserted at generation phase
     *
     * @return the byte array of the pdf generated.
     */
    public byte[] malfunctionLegalFact(LanguageEnum language, MalfunctionLegalFact malfunctionLegalFact) {
        return templateEngineClient.malfunctionLegalFact(language, malfunctionLegalFact);
    }
}
