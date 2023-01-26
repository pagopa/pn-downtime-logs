package it.pagopa.pn.downtime.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.downtime.pn_downtime_logs.api.StatusApi;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnStatusResponse;
import it.pagopa.pn.downtime.service.DowntimeLogsService;

@Validated
@RestController
public class StatusController implements StatusApi {

	@Autowired
	private DowntimeLogsService downtimeLogsService;

	@Override
	public ResponseEntity<PnStatusResponse> status() {
		PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();
		PnAuditLogEvent logEvent = auditLogBuilder.before(PnAuditLogEventType.AUD_NT_DOWTIME, "status").build();
		logEvent.log();
		PnStatusResponse openIncidents;
		try {
			openIncidents = downtimeLogsService.currentStatus();
			if (!openIncidents.getOpenIncidents().isEmpty()) {
				return ResponseEntity.internalServerError().body(openIncidents);
			}
			logEvent.generateSuccess().log();
		} catch (Exception exc) {
			logEvent.generateFailure("Exception on status =" + exc.getMessage()).log();
			throw exc;
		}
		return ResponseEntity.ok(openIncidents);
	}

}
