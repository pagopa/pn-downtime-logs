package it.pagopa.pn.downtime.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
@Api(tags = "API  HealthCheck")
@RestController
@Validated
@RequestMapping(value = "v1/downtime", produces = APPLICATION_JSON_VALUE, consumes = {
        MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE })
public class HeathCheckController {
	
    @GetMapping(value = "/healtcheck")
    public ResponseEntity<Object> checkReady() {
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
