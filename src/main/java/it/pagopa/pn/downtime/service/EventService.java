package it.pagopa.pn.downtime.service;

import java.util.List;

import it.pagopa.pn.downtime.model.Event;

/**
 * An interface containing all methods for allergies.
 */
public interface EventService {

	List<Event> getEvents();
}
