package it.pagopa.pn.downtime.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.LegalFactDownloadMetadataResponse;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.MalfunctionLegalFact;
import it.pagopa.pn.downtime.model.DowntimeLogs;


public interface LegalFactService {

	/**
	 * Generates the pdf of the legal fact and makes the reservation and the upload of the file.
	 *
	 * @param downtime the downtime used for the legal fact generation
	 * @return the downtime log updated with the legal fact id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws TemplateException the template exception
	 */
	DowntimeLogs generateLegalFact(DowntimeLogs downtime) throws IOException, NoSuchAlgorithmException, TemplateException;

	/**
	 * Generates the pdf of the legal for preview.
	 *
	 * @param malfunctionLegalFact the malfunctionLegalFact used for the legal fact generation
	 * @return the byte[] file
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws TemplateException the template exception
	 */
	byte[] previewLegalFact(MalfunctionLegalFact malfunctionLegalFact) throws IOException, NoSuchAlgorithmException, TemplateException;

	/**
	 * Gets the legal fact by making the call to SafeStorage.
	 *
	 * @param legalFactId the legal fact id
	 * @return the link for the download of the legal fact or the retry after for retrying the request
	 */
	LegalFactDownloadMetadataResponse getLegalFact(String legalFactId);


}
