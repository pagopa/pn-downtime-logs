package it.pagopa.pn.downtime.controller;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.pagopa.pn.downtime.components.schemas.PnDownTimeHistoryEntry;
import it.pagopa.pn.downtime.components.schemas.PnFunctionality;
import it.pagopa.pn.downtime.components.schemas.PnFunctionalityStatus;
import it.pagopa.pn.downtime.dto.response.PnDowntimeHistoryResponse;
import it.pagopa.pn.downtime.dto.response.PnStatusResponse;
import it.pagopa.pn.downtime.service.DownTimeService;

@Validated
@RestController
@RequestMapping("/downtime")
public class DownTimeController {

	private static final Logger logger = LoggerFactory.getLogger(DownTimeController.class);
	@Autowired
	private DownTimeService downTimeService;

	@GetMapping("/document")
	public ResponseEntity<String> getDocument(
			@RequestParam(name = "document") @NotNull(message = "documento nullo") String document) {
		logger.info("getDocument - INPUT DATA - document: " + document);
		return ResponseEntity.ok(downTimeService.getDocument(document));
	}

	@GetMapping("/status")
	public ResponseEntity<PnStatusResponse> getStatus() {
		logger.info("Sono entrato in status");
		return ResponseEntity.ok(downTimeService.getStatus());
	}
	
	@GetMapping("/history")
	public ResponseEntity<PnDowntimeHistoryResponse> getHistory(
		@RequestParam(name = "fromTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @NotNull(message = "Errore inserire fromTime") @Past(message ="La data deve essere passata") Date fromTime,
		@RequestParam(name = "toTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @NotNull(message = "Errore inserire toTime") @Past(message ="La data deve essere passata") Date toTime,
		@RequestParam(required=false, name = "functionality") List<PnFunctionality> functionality) {
		logger.info("Sono entrato in history");
		return ResponseEntity.ok(downTimeService.getHistory(fromTime, toTime, functionality));
	}
	
	
}
