package it.pagopa.pn.downtime.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

   
    private final EventRepository eventRepository;
	@Override
	public List<Event> getEvents() {

		log.info("getEvents");
		Pageable secondPageWithFiveElements = PageRequest.of(1, 5);
		Page<Event> allEventBy = eventRepository.findAllByFunctionality("NOTIFICATION_CREATE", secondPageWithFiveElements);
		
		return allEventBy.getContent();
	}


}
