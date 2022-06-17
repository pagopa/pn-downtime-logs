package it.pagopa.pn.downtime.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class DowntimeLogsServiceImpl implements DowntimeLogsService {

	@Autowired
	DowntimeLogsRepository downtimeLogsRepository;
	@Autowired
	DowntimeLogsMapper downtimeLogsMapper;

	@Override
	public PnDowntimeHistoryResponse getStatusHistory(OffsetDateTime fromTime, OffsetDateTime toTime,
			List<PnFunctionality> functionality, String page, String size) {

		log.info("getStatusHistory");

		Pageable pageRequest = PageRequest.of(Integer.valueOf(page), Integer.valueOf(size));

		Page<DowntimeLogs> pageHistory = downtimeLogsRepository
				.findAllByFunctionalityInAndStartDateGreaterThanEqualAndEndDateLessThanEqual(functionality, fromTime,
						toTime, pageRequest);

		List<DowntimeLogs> listHistory = pageHistory.getContent();
		List<PnDowntimeEntry> listResponse = new ArrayList<>();

		for (DowntimeLogs downtimeLogs : listHistory) {
			PnDowntimeEntry entry = downtimeLogsMapper.downtimeLogsToPnDowntimeEntry(downtimeLogs);
			listResponse.add(entry);
		}

		PnDowntimeHistoryResponse pn = new PnDowntimeHistoryResponse();

		pn.setNextPage(pageHistory.hasNext() ? Integer.valueOf(page) + 1 + "" : page);
		pn.setResult(listResponse);


		return pn;
	}

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
		return pnStatusResponseEntry;
	}
	
	@Override
	public void saveDowntimeLogs(String functionalityStartYear, OffsetDateTime startDate,
			PnFunctionality functionality, PnFunctionalityStatus status, String startEventUuid, String uuid) {
		DowntimeLogs downtimeLogs = new DowntimeLogs();
		downtimeLogs.setFunctionalityStartYear(functionalityStartYear);
		downtimeLogs.setStartDate(startDate);
		downtimeLogs.setStatus(status);
		downtimeLogs.setStartEventUuid(startEventUuid);
		downtimeLogs.setFunctionality(functionality);
		downtimeLogs.setUuid(uuid);
		downtimeLogsRepository.save(downtimeLogs);
	}
}
