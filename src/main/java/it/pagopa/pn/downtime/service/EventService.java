package it.pagopa.pn.downtime.service;

import it.pagopa.pn.downtime.model.Event;

/**
 * An interface containing all methods for allergies.
 */
public interface EventService {

	Iterable<Event> getEvents();

}
