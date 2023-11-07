package it.pagopa.pn.downtime.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import it.pagopa.pn.downtime.generated.openapi.server.v1.api.HealthCheckApi;




@RestController
@Validated
public class HeathCheckController implements HealthCheckApi {

	@Override
	public ResponseEntity<Void> healthcheck() {
		return new ResponseEntity<>(HttpStatus.OK);

	}
}
