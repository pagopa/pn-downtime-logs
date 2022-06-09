package it.pagopa.pn.downtime.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.repository.EventRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
    private final EventRepository eventRepository;
	@Override
	public Iterable<Event> getEvents() {
		
		return eventRepository.findAll();
	}

}
