package it.pagopa.pn.downtime.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.model.DowntimeLogsId;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;


@EnableScanCount
@EnableScan
public interface DowntimeLogsRepository extends PagingAndSortingRepository<DowntimeLogs, DowntimeLogsId> {

	Page<DowntimeLogs> findAllByFunctionalityInAndStartDateGreaterThanEqualAndEndDateLessThanEqual(List<PnFunctionality> functionality,
			OffsetDateTime fromTime, OffsetDateTime toTime, Pageable pageRequest);

	DowntimeLogs findByFunctionalityAndEndDateIsNull(PnFunctionality functionality);
	
	List<DowntimeLogs> findByFunctionalityAndStartDateLessThanEqual(PnFunctionality functionality, OffsetDateTime timestamp);
	

}