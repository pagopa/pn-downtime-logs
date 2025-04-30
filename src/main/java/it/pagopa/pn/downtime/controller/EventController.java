package it.pagopa.pn.downtime.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.downtime.mapper.DowntimeLogsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import freemarker.template.TemplateException;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.downtime.generated.openapi.server.v1.api.DowntimeApi;
import it.pagopa.pn.downtime.generated.openapi.server.v1.api.DowntimeInternalApi;
import it.pagopa.pn.downtime.service.DowntimeLogsService;
import it.pagopa.pn.downtime.service.EventService;
import it.pagopa.pn.downtime.service.LegalFactService;

@Validated
@RestController
public class EventController implements DowntimeApi, DowntimeInternalApi {

	private static final Logger log = LoggerFactory.getLogger(EventController.class);
	@Autowired
	private EventService eventService;

	@Autowired
	private LegalFactService legalFactService;

	@Autowired
	private DowntimeLogsService downtimeLogsService;

	/**
	 * Current status.
	 *
	 * @return all functionalities and the open downtimes
	 */
	@Override
	public ResponseEntity<PnStatusResponse> currentStatus() {
		return ResponseEntity.ok(downtimeLogsService.currentStatus());
	}

	/**
	 * Adds the status change event.
	 *
	 * @param xPagopaPnUid        the x pagopa pn uid. Required
	 * @param pnStatusUpdateEvent the input for the new event. Required
	 * @return the response entity
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws IOException              Signals that an I/O exception has occurred.
	 * @throws TemplateException        the template exception
	 */
	@Override
	public ResponseEntity<Void> addStatusChangeEvent(String xPagopaPnUid, List<PnStatusUpdateEvent> pnStatusUpdateEvent) {
		PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();
        PnAuditLogEvent logEvent = auditLogBuilder.before(PnAuditLogEventType.AUD_NT_INSERT,
                "addStatusChangeEvent - xPagopaPnUid={}, Current date(GMT/UTC)={}", xPagopaPnUid, OffsetDateTime.now())
				.mdcEntry("uid", xPagopaPnUid).build();
		
		logEvent.log();
		try {
			eventService.addStatusChangeEvent(xPagopaPnUid, pnStatusUpdateEvent);
			logEvent.generateSuccess().log();
		} catch (IOException exc) {
			logEvent.generateFailure("Exception on addStatusChangeEvent: " + exc.getMessage()).log();
		}
		return ResponseEntity.noContent().build();
	}

	/**
	 * Gets the legal fact.
	 *
	 * @param legalFactId the legal fact id. Required
	 * @return the link for the download of the legal fact or the retry after for
	 *         retrying the request
	 */
	@Override
	public ResponseEntity<LegalFactDownloadMetadataResponse> getLegalFact(String legalFactId) {
		return ResponseEntity.ok(legalFactService.getLegalFact(legalFactId));
	}

	/**
	 * Status history.
	 *
	 * @param fromTime      starting timestamp of the research. Required
	 * @param toTime        ending timestamp of the research
	 * @param functionality functionalities for which the research has to be done
	 * @param page          the page of the research
	 * @param size          the size of the researcj
	 * @return all the downtimes present in the period of time specified
	 */
	@Override
	public ResponseEntity<PnDowntimeHistoryResponse> statusHistory(OffsetDateTime fromTime, OffsetDateTime toTime,
			List<PnFunctionality> functionality, String page, String size) {
		return ResponseEntity.ok(downtimeLogsService.getStatusHistory(fromTime, toTime, functionality, page, size));
	}

	@Override
	public ResponseEntity<PnDowntimeHistoryResponse> getResolved(Integer year, Integer month) {
		log.info("Get resolved with year={} and month={}", year, month);
		return ResponseEntity.ok(downtimeLogsService.getResolved(year, month));
	}

	@Override
	public Optional<NativeWebRequest> getRequest() {
		return DowntimeApi.super.getRequest();
	}

	@Override
	public ResponseEntity<Resource> getMalfunctionPreview(MalfunctionLegalFact malfunctionLegalFact) {
        try {
			ByteArrayResource resource = new ByteArrayResource(legalFactService.previewLegalFact(malfunctionLegalFact));
			return ResponseEntity.ok(resource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
