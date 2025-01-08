package it.pagopa.pn.downtime.middleware.legalfactgenerator;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import java.io.IOException;


public interface LegalFactGenerator {
    /**
     * Generates the pdf of the malfunction legal fact.
     *
     * @param downtimeLogs the downtime used for the legal fact generation.
     * @return the byte array of the pdf generated.
     *
     * @throws IOException              Signals that an I/O exception has occurred.
     * @throws TemplateException        the template exception
     */
    byte[] generateMalfunctionLegalFact(DowntimeLogs downtimeLogs) throws IOException, TemplateException;
}