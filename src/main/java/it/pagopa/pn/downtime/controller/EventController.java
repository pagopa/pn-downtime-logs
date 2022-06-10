package it.pagopa.pn.downtime.controller;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.pn_downtime.api.DowntimeApi;
import it.pagopa.pn.downtime.pn_downtime.model.LegalFactDownloadMetadataResponse;
import it.pagopa.pn.downtime.pn_downtime.model.PnDowntimeHistoryResponse;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusResponse;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.service.EventService;

@Validated
@RestController
public class EventController  implements DowntimeApi{

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    @Autowired
    private EventService eventService;
    
    @Override
    public ResponseEntity<List<PnStatusResponse>> currentStatus() {
		return ResponseEntity.ok(null);
    	
    }
    
    @Override
    public ResponseEntity<Void> addStatusChangeEvent(String xPagopaPnUid, List<PnStatusUpdateEvent> pnStatusUpdateEvent){
        return ResponseEntity.ok(null);

    }
    @Override
    public ResponseEntity<LegalFactDownloadMetadataResponse> getLegalFact( String legalFactId) {
    	  return ResponseEntity.ok(null);

    }
    @Override
    public ResponseEntity<List<PnDowntimeHistoryResponse>> statusHistory(OffsetDateTime fromTime,OffsetDateTime toTime, List<PnFunctionality> functionality, String page, String size) {
    	  return ResponseEntity.ok(null);
    }
 
    
    

    @ApiOperation(value = "Event prova", notes = "The function returns all the events")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "This is a bad request, please stick to the API description",
                    response = Object.class),
            @ApiResponse(code = 404, message = "Resource Not Found", response = Object.class),
            @ApiResponse(code = 409, message = "Conflict", response = Object.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = Object.class) })
    @GetMapping("/prova")
    public ResponseEntity<List<Event>> getEvents() {
        logger.info("getEvents");
        return ResponseEntity.ok(eventService.getEvents());
    }


}
