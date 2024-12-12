package it.pagopa.pn.downtime.middleware.legalfactgenerator;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.model.DowntimeLogs;

import java.io.IOException;

public interface LegalFactGenerator {

    byte[] generateMalfunctionLegalFact(DowntimeLogs downtimeLogs) throws IOException, TemplateException;

}