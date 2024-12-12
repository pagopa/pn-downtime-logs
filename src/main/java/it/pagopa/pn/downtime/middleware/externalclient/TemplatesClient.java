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

    public byte[] malfunctionLegalFact(LanguageEnum language, MalfunctionLegalFact malfunctionLegalFact) {
        return templateEngineClient.malfunctionLegalFact(language, malfunctionLegalFact);
    }
}
