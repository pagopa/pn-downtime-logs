package it.pagopa.pn.downtime.service;

import java.io.IOException;
import java.util.List;

import it.pagopa.pn.downtime.pn_downtime_logs.model.PnStatusUpdateEvent;


/**
 * An interface containing all methods for allergies.
 */
public interface EventService {
	
	/**
	 * Adds the status change event.
	 *
	 * @param xPagopaPnUid the x pagopa pn uid. Required
	 * @param pnStatusUpdateEvent the input for the new event. Required
	 * @return the void
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	Void addStatusChangeEvent(String xPagopaPnUid, List<PnStatusUpdateEvent> pnStatusUpdateEvent) throws  IOException;
}
