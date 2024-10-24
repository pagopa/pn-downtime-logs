package it.pagopa.pn.downtime.service;

import java.time.OffsetDateTime;
import java.util.List;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnDowntimeHistoryResponse;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusResponse;
import it.pagopa.pn.downtime.model.DowntimeLogs;



public interface DowntimeLogsService {

	/**
	 * Gets the status history.
	 *
	 * @param fromTime starting timestamp of the research. Required
	 * @param toTime ending timestamp of the research
	 * @param functionality functionalities for which the research has to be done
	 * @param page the page of the research
	 * @param size the size of the researcj
	 * @return all the downtimes present in the period of time specified
	 */
	PnDowntimeHistoryResponse getStatusHistory(OffsetDateTime fromTime, OffsetDateTime toTime,
			List<PnFunctionality> functionality, String page, String size);

	/**
	 * Current status.
	 *
	 * @return all functionalities and the open downtimes
	 */
	PnStatusResponse currentStatus();

	/**
	 * Save a new downtime logs.
	 *
	 * @param functionalityStartYear the functionality start year which is a comination of functionality and the yeat of the startDate
	 * @param startDate the start date
	 * @param functionality the functionality
	 * @param startEventUuid the uuid of start event
	 * @param uuid the uuid
	 */
	void saveDowntimeLogs(String functionalityStartYear, OffsetDateTime startDate, PnFunctionality functionality,
			String startEventUuid, String uuid);

	List<DowntimeLogs> findAllByEndDateIsNotNullAndLegalFactIdIsNull();


	PnDowntimeHistoryResponse getResolved(Integer year, Integer month);
}
