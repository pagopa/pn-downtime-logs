package it.pagopa.pn.downtime.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime.model.PnDowntimeHistoryResponse;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
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
	public List<PnDowntimeHistoryResponse> getStatusHistory(OffsetDateTime fromTime, OffsetDateTime toTime, List<PnFunctionality> functionality,
			String page, String size) {
		
		log.info("getStatusHistory");
		
		Pageable pageRequest = PageRequest.of(Integer.valueOf(page), Integer.valueOf(size));
		
		Page<DowntimeLogs> pageHistory = downtimeLogsRepository
				.findByFunctionalityInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(functionality, fromTime, toTime, pageRequest);
			
		List<DowntimeLogs> listHistory = pageHistory.getContent();
		
		PnDowntimeHistoryResponse pn = new PnDowntimeHistoryResponse();
		pn.setNextPage(page);
		pn.setResult(null);
		
		//TODO!
		
		return null;
	}
}
