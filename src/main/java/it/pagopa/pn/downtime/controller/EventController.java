package it.pagopa.pn.downtime.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.pn_downtime.api.DowntimeApi;
import it.pagopa.pn.downtime.pn_downtime.model.LegalFactDownloadMetadataResponse;
import it.pagopa.pn.downtime.pn_downtime.model.PnDowntimeHistoryResponse;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusResponse;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.service.DowntimeLogsService;
import it.pagopa.pn.downtime.service.EventService;
import it.pagopa.pn.downtime.service.LegalFactService;

@Validated
@RestController
public class EventController implements DowntimeApi {

	@Autowired
	private EventService eventService;
	@Autowired
	private LegalFactService legalFactService;
	@Autowired
	private DowntimeLogsService downtimeLogsService;

	@Override
	public ResponseEntity<PnStatusResponse> currentStatus() {
		return ResponseEntity.ok(downtimeLogsService.currentStatus());

	}

	@Override
	public ResponseEntity<Void> addStatusChangeEvent(String xPagopaPnUid,
			List<PnStatusUpdateEvent> pnStatusUpdateEvent) throws NoSuchAlgorithmException, IOException, TemplateException {
		return ResponseEntity.ok(eventService.addStatusChangeEvent(xPagopaPnUid, pnStatusUpdateEvent));

	}

	@Override
	public ResponseEntity<LegalFactDownloadMetadataResponse> getLegalFact(String legalFactId) {
	    return ResponseEntity.ok(legalFactService.getLegalFact(legalFactId));
	}

	@Override
	public ResponseEntity<PnDowntimeHistoryResponse> statusHistory(OffsetDateTime fromTime, OffsetDateTime toTime,
			List<PnFunctionality> functionality, String page, String size) {
		return ResponseEntity.ok(downtimeLogsService.getStatusHistory(fromTime, toTime, functionality, page, size));
	}

}
