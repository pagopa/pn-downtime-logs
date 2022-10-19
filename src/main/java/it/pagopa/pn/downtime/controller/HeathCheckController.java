package it.pagopa.pn.downtime.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import it.pagopa.pn.downtime.pn_downtime_logs.api.HealtcheckApi;
@RestController
@Validated
public class HeathCheckController implements HealtcheckApi{
	
	@Override
    public ResponseEntity<Void> healtcheck() {
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
