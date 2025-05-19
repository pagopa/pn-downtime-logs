package it.pagopa.pn.downtime.controller;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.downtime.generated.openapi.server.v1.api.DowntimeBoApi;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.BoStatusUpdateEvent;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionalityStatus;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
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
        log.info("Get malfunction preview for xPagopaPnUid: {}, event: {}", xPagopaPnUid, boStatusUpdateEvent);
        try {
            PnStatusUpdateEvent event = mapBoEventToPnEvent(xPagopaPnUid, boStatusUpdateEvent);
            byte[] data = eventService.previewLegalFact(event);
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<Void> addStatusChangeEventBo(String xPagopaPnUid, BoStatusUpdateEvent boStatusUpdateEvent) {
        PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();
        PnAuditLogEvent logEvent = auditLogBuilder.before(PnAuditLogEventType.AUD_NT_INSERT,
                        "addStatusChangeEvent from backoffice - xPagopaPnUid={}, boStatusUpdateEvent={}, Current date(GMT/UTC)={}", xPagopaPnUid, boStatusUpdateEvent, OffsetDateTime.now())
                .mdcEntry("uid", xPagopaPnUid).build();

        logEvent.log();

        try {
            if (boStatusUpdateEvent.getStatus() == PnFunctionalityStatus.OK && boStatusUpdateEvent.getHtmlDescription() == null || boStatusUpdateEvent.getHtmlDescription().isEmpty()) {
                throw new IllegalArgumentException("HtmlDescription cannot be null or empty when status is OK");
            }

            PnStatusUpdateEvent pnStatusUpdateEvent = mapBoEventToPnEvent(xPagopaPnUid, boStatusUpdateEvent);

            List<PnStatusUpdateEvent> events = new ArrayList<>();
            events.add(pnStatusUpdateEvent);

            eventService.addStatusChangeEvent(xPagopaPnUid, events);
            logEvent.generateSuccess().log();

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logEvent.generateFailure("Exception on addStatusChangeEvent from backoffice: " + e.getMessage()).log();
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