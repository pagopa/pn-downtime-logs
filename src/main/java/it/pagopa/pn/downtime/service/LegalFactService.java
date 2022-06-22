package it.pagopa.pn.downtime.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime.model.LegalFactDownloadMetadataResponse;

/**
 * An interface containing all methods for allergies.
 */
public interface LegalFactService {

	DowntimeLogs generateLegalFact(DowntimeLogs downtime) throws IOException, NoSuchAlgorithmException, TemplateException;

	LegalFactDownloadMetadataResponse getLegalFact(String legalFactId);

	DowntimeLogs reserveUploadFile(byte[] file, DowntimeLogs downtime) throws NoSuchAlgorithmException;

}
