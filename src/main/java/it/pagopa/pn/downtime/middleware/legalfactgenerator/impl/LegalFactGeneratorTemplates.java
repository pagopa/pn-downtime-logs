package it.pagopa.pn.downtime.middleware.legalfactgenerator.impl;

import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.model.LanguageEnum;
import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.model.MalfunctionLegalFact;
import it.pagopa.pn.downtime.middleware.externalclient.TemplatesClient;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.LegalFactGenerator;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.util.OffsetDateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@AllArgsConstructor
public class LegalFactGeneratorTemplates implements LegalFactGenerator {

    private final TemplatesClient templatesClient;

    /**
     * Generates the pdf of the malfunction legal fact.
     * This method is invoked to generate the legal fact.
     * It populates all field with provided data then the client invoke the external service.
     *
     * @param downtimeLogs the downtime used for the legal fact generation
     *
     * @return the byte array of the pdf generated.
     */
    @Override
    public byte[] generateMalfunctionLegalFact(DowntimeLogs downtimeLogs) {
        log.info("retrieve MalfunctionLegalFact template");
        MalfunctionLegalFact malfunctionLegalFact = new MalfunctionLegalFact()
                .startDate(OffsetDateTimeFormatter.getDateFormatted(downtimeLogs.getStartDate()))
                .timeReferenceStartDate(OffsetDateTimeFormatter.getTimeFormatted(downtimeLogs.getStartDate()))
                .endDate(OffsetDateTimeFormatter.getDateFormatted(downtimeLogs.getEndDate()))
                .timeReferenceEndDate(OffsetDateTimeFormatter.getTimeFormatted(downtimeLogs.getEndDate()));
        return templatesClient.malfunctionLegalFact(LanguageEnum.IT, malfunctionLegalFact);
    }
}