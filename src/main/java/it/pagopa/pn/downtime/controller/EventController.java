package it.pagopa.pn.downtime.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.service.EventService;

@Validated
@RestController
@RequestMapping("/downtime")
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    @Autowired
    private EventService eventService;


    @GetMapping("/prova")
    public ResponseEntity<Iterable<Event>> getAllergies() {
        logger.info("getAllergies - INPUT DATA - idAssistito: ");
        return ResponseEntity.ok(eventService.getEvents());
    }


}
