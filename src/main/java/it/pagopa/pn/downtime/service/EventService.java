package it.pagopa.pn.downtime.service;

import java.util.List;

import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;

/**
 * An interface containing all methods for allergies.
 */
public interface EventService {
	
	Void addStatusChangeEvent(String xPagopaPnUid, List<PnStatusUpdateEvent> pnStatusUpdateEvent);
}
