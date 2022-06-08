package it.pagopa.pn.downtime.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import it.pagopa.pn.downtime.components.schemas.PnDownTimeHistoryEntry;
import it.pagopa.pn.downtime.components.schemas.PnFunctionality;
import it.pagopa.pn.downtime.components.schemas.PnFunctionalityStatus;
import it.pagopa.pn.downtime.dto.response.PnDowntimeHistoryResponse;
import it.pagopa.pn.downtime.dto.response.PnStatusResponse;
import it.pagopa.pn.downtime.service.DownTimeService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DownTimeServiceImpl implements DownTimeService {
	
	//@Autowired
    //private final DownTimeRepository downTimeRepository;
	
	@Override
	public String getDocument(String document) {
		//return downTimeRepository.findByNomeDocumento(documento);
		return "prova";
	}

	@Override
	public PnStatusResponse getStatus() {
		PnStatusResponse pnResponse = new PnStatusResponse();
    	PnFunctionality[] pnFunctionality = new PnFunctionality[]{PnFunctionality.NOTIFICATION_CREATE, PnFunctionality.NOTIFICATION_VISUALIZZATION, PnFunctionality.NOTIFICATION_WORKFLOW};
    	pnResponse.setFunctionalities(pnFunctionality);
    	PnDownTimeHistoryEntry[] openIncidents = new PnDownTimeHistoryEntry[1];
    	PnDownTimeHistoryEntry incident = new PnDownTimeHistoryEntry();
    	incident.setFunctionality(PnFunctionality.NOTIFICATION_CREATE);
    	incident.setStatus(PnFunctionalityStatus.KO);
    	incident.setStartDate("2022-06-08 11:00:00");
    	openIncidents[0]=incident;
    	pnResponse.setOpenIncidents(openIncidents);
		return pnResponse;
	}

	@Override
	public PnDowntimeHistoryResponse getHistory(Date fromTime, Date toTime, List<PnFunctionality> functionality) {
		PnDowntimeHistoryResponse pnHistoryResponse = new PnDowntimeHistoryResponse();
		PnDownTimeHistoryEntry[] result = new PnDownTimeHistoryEntry[1];
    	PnDownTimeHistoryEntry incident = new PnDownTimeHistoryEntry();
    	incident.setFunctionality(PnFunctionality.NOTIFICATION_CREATE);
    	incident.setStatus(PnFunctionalityStatus.KO);
    	incident.setStartDate("2022-06-08 11:00:00");
    	result[0]=incident;
    	pnHistoryResponse.setResult(result);
    	pnHistoryResponse.setNextPage("2");
		return pnHistoryResponse;
	}

	
}
