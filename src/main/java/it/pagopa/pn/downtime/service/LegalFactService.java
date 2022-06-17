package it.pagopa.pn.downtime.service;

import it.pagopa.pn.downtime.pn_downtime.model.LegalFactDownloadMetadataResponse;

/**
 * An interface containing all methods for allergies.
 */
public interface LegalFactService {

  Integer reserveUploadFile();


  LegalFactDownloadMetadataResponse getLegalFact(String legalFactId);
	
}
