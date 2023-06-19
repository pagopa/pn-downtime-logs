package it.pagopa.pn.downtime.service;

import java.io.IOException;
import java.util.List;

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
}
