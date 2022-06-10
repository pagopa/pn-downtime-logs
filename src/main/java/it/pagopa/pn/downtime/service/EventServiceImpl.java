package it.pagopa.pn.downtime.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
	public List<Event> getEvents() {
		Pageable secondPageWithFiveElements = PageRequest.of(1, 5);
		Page<Event> allEventBy = eventRepository.findAllByFunctionality("NOTIFICATION_CREATE", secondPageWithFiveElements);
		
		return allEventBy.getContent();
	}

}
