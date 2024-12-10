package it.pagopa.pn.downtime.middleware.externalclient;

import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.model.LanguageEnum;
import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.model.MalfunctionLegalFact;


public interface TemplatesClient {
    byte[] malfunctionLegalFact(LanguageEnum language, MalfunctionLegalFact malfunctionLegalFact) ;
}
