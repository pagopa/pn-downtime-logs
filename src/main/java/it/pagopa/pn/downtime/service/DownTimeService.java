package it.pagopa.pn.downtime.service;

import java.util.Date;
import java.util.List;

import it.pagopa.pn.downtime.components.schemas.PnFunctionality;
import it.pagopa.pn.downtime.dto.response.PnDowntimeHistoryResponse;
import it.pagopa.pn.downtime.dto.response.PnStatusResponse;

/**
 * An interface containing all methods for allergies.
 */
public interface DownTimeService {

 
    String getDocument(String document);
    PnStatusResponse getStatus();
    PnDowntimeHistoryResponse getHistory(Date fromTime, Date toTime, List<PnFunctionality> functionality);
}
