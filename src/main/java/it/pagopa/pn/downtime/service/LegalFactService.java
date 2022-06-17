package it.pagopa.pn.downtime.service;

import java.io.IOException;

import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime.model.LegalFactDownloadMetadataResponse;

/**
 * An interface containing all methods for allergies.
 */
public interface LegalFactService {

	DowntimeLogs generateLegalFact(DowntimeLogs downtime) throws IOException;

	LegalFactDownloadMetadataResponse getLegalFact(String legalFactId);

	DowntimeLogs reserveUploadFile(byte[] file, DowntimeLogs downtime);

}
