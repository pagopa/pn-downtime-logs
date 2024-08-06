package it.pagopa.pn.downtime.controller;

import it.pagopa.pn.downtime.generated.openapi.server.v1.api.InteropProbingApi;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusResponse;
import it.pagopa.pn.downtime.service.DowntimeLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;


@RestController
public class InteropProbingController implements InteropProbingApi {

    @Autowired
    private DowntimeLogsService downtimeLogsService;

    @Override
    public ResponseEntity<Void> getEserviceStatus() {
        PnStatusResponse openIncidents = downtimeLogsService.currentStatus();
        if (!openIncidents.getOpenIncidents().isEmpty()) {
            throw new RestClientException("There is a problem processing the request on the server.");
        } else
            return new ResponseEntity<>(HttpStatus.OK);
    }
}
