package it.pagopa.pn.downtime.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import it.pagopa.pn.downtime.mapper.DowntimeLogsMapper;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime.model.PnDowntimeEntry;
import it.pagopa.pn.downtime.pn_downtime.model.PnDowntimeHistoryResponse;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusResponse;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * The Class DowntimeLogsServiceImpl.
 */
@Service

/**
 * Instantiates a new downtime logs service impl.
 */
@RequiredArgsConstructor

/** The Constant log. */
@Slf4j
public class DowntimeLogsServiceImpl implements DowntimeLogsService {

	/** The downtime logs repository. */
	@Autowired
	DowntimeLogsRepository downtimeLogsRepository;
	
	/** The downtime logs mapper. */
	@Autowired
	DowntimeLogsMapper downtimeLogsMapper;

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
	@Override
	public PnDowntimeHistoryResponse getStatusHistory(OffsetDateTime fromTime, OffsetDateTime toTime,
			List<PnFunctionality> functionality, String page, String size) {

		log.info("getStatusHistory - Input - fromTime: " + fromTime.toString() + " toTime: "
				+ (toTime != null ? toTime.toString() : "") + " functionality: "
				+ (functionality != null ? functionality.toString() : "") + " page: " + page + " size: " + size);

		List<DowntimeLogs> listHistoryResults = getStatusHistoryResults(fromTime, toTime, functionality);

		Page<DowntimeLogs> pageHistory = null;

		if (page != null && !page.isEmpty() && size != null && !size.isEmpty()) {

			List<DowntimeLogs> listHistorySubList = new ArrayList<>();

			if (Integer.valueOf(size) * Integer.valueOf(page) <= listHistoryResults.size()) {
				listHistorySubList = listHistoryResults.subList(Integer.valueOf(size) * Integer.valueOf(page),
						Integer.valueOf(size) * Integer.valueOf(page) + Integer.valueOf(size)
								- 1 < listHistoryResults.size() - 1
										? Integer.valueOf(size) * Integer.valueOf(page) + Integer.valueOf(size)
										: listHistoryResults.size());
			}

			Pageable pageRequest = PageRequest.of(Integer.valueOf(page), Integer.valueOf(size));

			pageHistory = new PageImpl<>(listHistorySubList, pageRequest, listHistoryResults.size());
		}

		List<PnDowntimeEntry> listResponse = new ArrayList<>();

		for (DowntimeLogs downtimeLogs : pageHistory != null ? pageHistory.getContent() : listHistoryResults) {
			PnDowntimeEntry entry = downtimeLogsMapper.downtimeLogsToPnDowntimeEntry(downtimeLogs);
			listResponse.add(entry);
		}

		PnDowntimeHistoryResponse pn = new PnDowntimeHistoryResponse();

		pn.setNextPage(pageHistory != null && pageHistory.hasNext() ? Integer.valueOf(page) + 1 + "" : page);
		pn.setResult(listResponse);

		log.info("Response: " + pn.toString());
		return pn;
	}

	/**
	 * Executes the queries for the getStatusHistory service
	 *
	 * @param fromTime starting timestamp of the research. Required
	 * @param toTime ending timestamp of the research
	 * @param functionality functionalities for which the research has to be done
	 * @return the combined results of the queries
	 */
	public List<DowntimeLogs> getStatusHistoryResults(OffsetDateTime fromTime, OffsetDateTime toTime,
			List<PnFunctionality> functionality) {

		List<DowntimeLogs> listHistoryStartDate = null;
		List<DowntimeLogs> listHistoryEndDate = null;
		List<DowntimeLogs> listHistory = new ArrayList<>();
		
		if (toTime != null) {
			listHistoryStartDate = downtimeLogsRepository.findAllByFunctionalityInAndStartDateBetween(
					functionality != null ? functionality : Arrays.asList(PnFunctionality.values()), fromTime, toTime);

			listHistoryEndDate = downtimeLogsRepository.findAllByFunctionalityInAndEndDateBetweenAndStartDateBefore(
					functionality != null ? functionality : Arrays.asList(PnFunctionality.values()), fromTime, toTime,
					fromTime);
		} else {
			listHistoryStartDate = downtimeLogsRepository.findAllByFunctionalityInAndStartDateAfter(
					functionality != null ? functionality : Arrays.asList(PnFunctionality.values()), fromTime);
			
			listHistoryEndDate = downtimeLogsRepository.findAllByFunctionalityInAndEndDateAfterAndStartDateBefore(
					functionality != null ? functionality : Arrays.asList(PnFunctionality.values()), fromTime,
					fromTime);

		}

		listHistory.addAll(listHistoryStartDate);

		listHistory.addAll(listHistoryEndDate);

		return listHistory;
	}

	/**
	 * Current status.
	 *
	 * @return all functionalities and the open downtimes
	 */
	@Override
	public PnStatusResponse currentStatus() {
		List<PnDowntimeEntry> openIncidents = new ArrayList<>();
		PnStatusResponse pnStatusResponseEntry = new PnStatusResponse();
		for (PnFunctionality pn : PnFunctionality.values()) {
			DowntimeLogs downtimeLogsDateNull = downtimeLogsRepository.findByFunctionalityAndEndDateIsNull(pn);
			if (downtimeLogsDateNull != null && downtimeLogsDateNull.getStatus().equals(PnFunctionalityStatus.KO)) {
				PnDowntimeEntry incident = downtimeLogsMapper.downtimeLogsToPnDowntimeEntry(downtimeLogsDateNull);
				openIncidents.add(incident);
			}
		}
		pnStatusResponseEntry.setFunctionalities(Arrays.asList(PnFunctionality.values()));
		pnStatusResponseEntry.setOpenIncidents(openIncidents);
		log.info("Response: " + pnStatusResponseEntry.toString());
		return pnStatusResponseEntry;
	}

	/**
	 * Save a new downtime logs.
	 *
	 * @param functionalityStartYear the functionality start year which is a comination of functionality and the yeat of the startDate
	 * @param startDate the start date
	 * @param functionality the functionality
	 * @param startEventUuid the uuid of start event
	 * @param uuid the uuid
	 */
	@Override
	public void saveDowntimeLogs(String functionalityStartYear, OffsetDateTime startDate, PnFunctionality functionality,
			String startEventUuid, String uuid) {
		DowntimeLogs downtimeLogs = new DowntimeLogs();
		downtimeLogs.setFunctionalityStartYear(functionalityStartYear);
		downtimeLogs.setStartDate(startDate);
		downtimeLogs.setStatus(PnFunctionalityStatus.KO);
		downtimeLogs.setStartEventUuid(startEventUuid);
		downtimeLogs.setFunctionality(functionality);
		downtimeLogs.setUuid(uuid);
		downtimeLogs.setFileAvailable(false);
		downtimeLogsRepository.save(downtimeLogs);
	}
	
	@Override
	public List<DowntimeLogs> findAllByEndDateIsNotNullAndLegalFactIdIsNull() {
		return downtimeLogsRepository.findAllByEndDateIsNotNullAndLegalFactIdIsNull();
	}
}
