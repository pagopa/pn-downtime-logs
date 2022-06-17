package it.pagopa.pn.downtime.service;

import java.time.OffsetDateTime;
import java.util.List;

import it.pagopa.pn.downtime.pn_downtime.model.PnDowntimeHistoryResponse;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusResponse;

/**
 * An interface containing all methods for allergies.
 */
public interface DowntimeLogsService {

	List<PnDowntimeHistoryResponse> getStatusHistory(OffsetDateTime fromTime, OffsetDateTime toTime,
			List<PnFunctionality> functionality, String page, String size);

	List<PnStatusResponse> currentStatus();

	void saveDowntimeLogs(String functionalityStartYear, OffsetDateTime startDate, PnFunctionality functionality,
			PnFunctionalityStatus status, String startEventUuid, String uuid);
}
