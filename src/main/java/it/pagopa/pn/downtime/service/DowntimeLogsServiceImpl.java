package it.pagopa.pn.downtime.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mapstruct.factory.Mappers;
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

	@Override
	public List<PnDowntimeHistoryResponse> getStatusHistory(OffsetDateTime fromTime, OffsetDateTime toTime,
			List<PnFunctionality> functionality, String page, String size) {

		log.info("getStatusHistory");

		Pageable pageRequest = PageRequest.of(Integer.valueOf(page), Integer.valueOf(size));
		
		Page<DowntimeLogs> pageHistory = downtimeLogsRepository
				.findAllByFunctionalityInAndStartDateGreaterThanEqualAndEndDateLessThanEqual(functionality, fromTime, toTime, pageRequest);
		
		List<DowntimeLogs> listHistory = pageHistory.getContent();
		List<PnDowntimeEntry> listResponse = new ArrayList<>();

		for (DowntimeLogs downtimeLogs : listHistory) {	
			
			DowntimeLogsMapper downtimeLogsMapper = Mappers.getMapper(DowntimeLogsMapper.class);
			PnDowntimeEntry entry = downtimeLogsMapper.downtimeLogsToPnDowntimeEntry(downtimeLogs);
			listResponse.add(entry);
		}

		PnDowntimeHistoryResponse pn = new PnDowntimeHistoryResponse();
		
		pn.setNextPage(pageHistory.hasNext() ? Integer.valueOf(page)+1+"" : page);
		pn.setResult(listResponse);
		
		List<PnDowntimeHistoryResponse> listPn = new ArrayList<>();
		listPn.add(pn);

		return listPn;
	}

	@Override
	public List<PnStatusResponse> currentStatus() {
		List<PnStatusResponse> pnStatusResponse = new ArrayList<>();
		List<PnDowntimeEntry> openIncidents = new ArrayList<>();

		PnStatusResponse pnStatusResponseEntry = new PnStatusResponse();
		for (PnFunctionality pn : PnFunctionality.values()) {
			DowntimeLogs downtimeLogsDateNull = downtimeLogsRepository.findByFunctionalityAndEndDateIsNull(pn);
			if (downtimeLogsDateNull != null && downtimeLogsDateNull.getStatus().equals(PnFunctionalityStatus.KO)) {
				DowntimeLogsMapper downtimeLogsMapper = Mappers.getMapper(DowntimeLogsMapper.class);
				PnDowntimeEntry incident = downtimeLogsMapper.downtimeLogsToPnDowntimeEntry(downtimeLogsDateNull);
				openIncidents.add(incident);
			}
		}
		pnStatusResponseEntry.setFunctionalities(Arrays.asList(PnFunctionality.values()));
		pnStatusResponseEntry.setOpenIncidents(openIncidents);
		pnStatusResponse.add(pnStatusResponseEntry);
		return pnStatusResponse;
	}
}
