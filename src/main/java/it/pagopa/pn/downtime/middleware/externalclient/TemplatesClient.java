package it.pagopa.pn.downtime.middleware.externalclient;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.api.TemplateApi;
import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.model.LanguageEnum;
import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.model.MalfunctionLegalFact;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
@CustomLog
public class TemplatesClient {

    private final TemplateApi templateEngineClient;

    /**
     * Generates the pdf of the malfunction legal fact.
     * <p>
     * This method is invoked to retrieve the legal fact from PnTemplatesEngine microservice.
     * </p>
     *
     * @param language is the language chosen for the generation of the legal fact.
     * @param malfunctionLegalFact is the dto that contains all document's data to be inserted at generation phase.
     * @return the byte array of the pdf generated.
     */
    public byte[] malfunctionLegalFact(LanguageEnum language, MalfunctionLegalFact malfunctionLegalFact) {
        Resource resource = templateEngineClient.malfunctionLegalFact(language, malfunctionLegalFact);
        try {
            return resource.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read malfunctionLegalFact response", e);
        }
    }
}
