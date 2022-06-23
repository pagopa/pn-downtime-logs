package it.pagopa.pn.downtime.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;

/**
 * An interface containing all methods for allergies.
 */
public interface EventService {
	
	Void addStatusChangeEvent(String xPagopaPnUid, List<PnStatusUpdateEvent> pnStatusUpdateEvent) throws NoSuchAlgorithmException, IOException, TemplateException;
}
