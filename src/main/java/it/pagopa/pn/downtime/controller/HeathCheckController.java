package it.pagopa.pn.downtime.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import it.pagopa.pn.downtime.pn_downtime_logs.api.HealthcheckApi;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnStatusResponse;
import it.pagopa.pn.downtime.service.DowntimeLogsService;

@RestController
@Validated
public class HeathCheckController implements HealthcheckApi {

	@Autowired
	private DowntimeLogsService downtimeLogsService;

	@Override
	public ResponseEntity<PnStatusResponse> status() {
		PnStatusResponse openIncidents = downtimeLogsService.currentStatus();
		if (!openIncidents.getOpenIncidents().isEmpty()) {
			return ResponseEntity.internalServerError().body(openIncidents);
		} else
			return ResponseEntity.ok(openIncidents);
	}
}
