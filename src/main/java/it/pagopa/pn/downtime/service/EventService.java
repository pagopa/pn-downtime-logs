package it.pagopa.pn.downtime.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusUpdateEvent;



public interface EventService {
	
	/**
	 * Adds the status change event.
	 *
	 * @param xPagopaPnUid the x pagopa pn uid. Required
	 * @param pnStatusUpdateEvent the input for the new event. Required
	 * @return the void
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void addStatusChangeEvent(String xPagopaPnUid, List<PnStatusUpdateEvent> pnStatusUpdateEvent) throws  IOException;

	/**
	 * Generates the pdf of the legal for preview.
	 *
	 * @param pnStatusUpdateEvent PnStatusUpdateEvent the malfunctionLegalFact used for the legal fact generation
	 * @return the byte[] file
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws TemplateException the template exception
	 */
	byte[] previewLegalFact(PnStatusUpdateEvent pnStatusUpdateEvent) throws IOException, NoSuchAlgorithmException, TemplateException;
}
