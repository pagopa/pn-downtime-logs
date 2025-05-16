package it.pagopa.pn.downtime.controller;

import it.pagopa.pn.downtime.generated.openapi.server.v1.api.DowntimeBoApi;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.BoStatusUpdateEvent;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Validated
@RestController
public class DowntimeBoApiController implements DowntimeBoApi {

    private static final Logger log = LoggerFactory.getLogger(DowntimeBoApiController.class);
    @Autowired
    private EventService eventService;

    @Override
    public ResponseEntity<Resource> getMalfunctionPreview(String xPagopaPnUid, BoStatusUpdateEvent boStatusUpdateEvent) {
        log.info("Get malfunction preview for event: {}", boStatusUpdateEvent);
        try {
            File file = new File("src/main/resources/sample.pdf");
            if (!file.exists()) {
                throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
            }

            Resource resource = new FileSystemResource("src/main/resources/sample.pdf");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PnStatusUpdateEvent mapBoEventToPnEvent(String xPagopaPnUid, BoStatusUpdateEvent boStatusUpdateEvent) {
        List<PnFunctionality> functionalities = new ArrayList<>();
        functionalities.add(boStatusUpdateEvent.getFunctionality());

        PnStatusUpdateEvent pnStatusUpdateEvent = new PnStatusUpdateEvent();
        pnStatusUpdateEvent.setStatus(boStatusUpdateEvent.getStatus());
        pnStatusUpdateEvent.setFunctionality(functionalities);
        pnStatusUpdateEvent.setTimestamp(boStatusUpdateEvent.getTimestamp());
        pnStatusUpdateEvent.setHtmlDescription(boStatusUpdateEvent.getHtmlDescription());

        pnStatusUpdateEvent.setSource(xPagopaPnUid);
        pnStatusUpdateEvent.setSourceType(PnStatusUpdateEvent.SourceTypeEnum.OPERATOR);

        return pnStatusUpdateEvent;
    }
}