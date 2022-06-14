package it.pagopa.pn.downtime.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

	private final EventRepository eventRepository;

	@Override
	public Void addStatusChangeEvent(String xPagopaPnUid, List<PnStatusUpdateEvent> pnStatusUpdateEvent) {
		log.info("addStatusChangeEvent");
		for (PnStatusUpdateEvent event : pnStatusUpdateEvent) {
			for (PnFunctionality functionality : event.getFunctionality()) {
				saveEvent(event.getTimestamp(), event.getTimestamp().toString().substring(0, 7).concat("-01"),
						functionality, event.getStatus(), event.getSourceType(), event.getSource());
			}
		}
		return null;
	}

	public void saveEvent(OffsetDateTime timestamp, String yearMonth, PnFunctionality functionality,
			PnFunctionalityStatus status, SourceTypeEnum sourceType, String source) {
		log.info("addStatusChangeEvent");
		Event event = new Event();
		event.setTimestamp(timestamp);
		event.setYearMonth(yearMonth);
		event.setFunctionality(functionality);
		event.setStatus(status);
		event.setSourceType(sourceType);
		event.setSource(source);
		eventRepository.save(event);

	}

}
