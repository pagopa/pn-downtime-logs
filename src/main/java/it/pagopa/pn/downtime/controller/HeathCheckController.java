package it.pagopa.pn.downtime.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import it.pagopa.pn.downtime.pn_downtime_logs.api.HealthcheckApi;

@RestController
@Validated
public class HeathCheckController implements HealthcheckApi{
	
	@Override
    public ResponseEntity<Void> healthcheck() {
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
