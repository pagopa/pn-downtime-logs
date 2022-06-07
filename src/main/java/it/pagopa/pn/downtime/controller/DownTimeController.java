package it.pagopa.pn.downtime.controller;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.pagopa.pn.downtime.service.DownTimeService;



@Validated
@RestController
@RequestMapping("/downtime")
public class DownTimeController {

    private static final Logger logger = LoggerFactory.getLogger(DownTimeController.class);
    @Autowired
    private DownTimeService downTimeService;

    @GetMapping("/document")
    public ResponseEntity<String> getDocument(@RequestParam(name = "document") @NotNull(
            message = "documento nullo")  String document) {
        logger.info("getDocument - INPUT DATA - document: " + document);
        return ResponseEntity.ok(downTimeService.getDocument(document));
    }
    



  
}
