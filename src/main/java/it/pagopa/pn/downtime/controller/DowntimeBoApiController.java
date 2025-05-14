package it.pagopa.pn.downtime.controller;

import it.pagopa.pn.downtime.generated.openapi.server.v1.api.DowntimeBoApi;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class DowntimeBoApiController implements DowntimeBoApi {

    private static final Logger log = LoggerFactory.getLogger(DowntimeBoApiController.class);
    @Autowired
    private EventService eventService;

    @Override
    public ResponseEntity<Resource> getMalfunctionPreview(PnStatusUpdateEvent pnStatusUpdateEvent) {
        log.info("Get malfunction preview for event: {}", pnStatusUpdateEvent);
        try {
            byte[] data = eventService.previewLegalFact(pnStatusUpdateEvent);
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .contentLength(data.length)
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ao3-malfunction-preview.pdf\"")
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}