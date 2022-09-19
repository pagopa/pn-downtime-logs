package it.pagopa.pn.downtime.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.springframework.data.repository.PagingAndSortingRepository;

import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.model.DowntimeLogsId;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionality;


/**
 * The Interface DowntimeLogsRepository.
 */
@EnableScanCount
@EnableScan
public interface DowntimeLogsRepository extends PagingAndSortingRepository<DowntimeLogs, DowntimeLogsId> {

	/**
	 * Find all by functionality in and start date between.
	 *
	 * @param functionality the functionality
	 * @param fromTime the from time
	 * @param toTime the to time
	 * @return the list
	 */
	List<DowntimeLogs> findAllByFunctionalityInAndStartDateBetween(List<PnFunctionality> functionality,
			OffsetDateTime fromTime, OffsetDateTime toTime);

	/**
	 * Find all by functionality in and end date between and start date before.
	 *
	 * @param functionality the functionality
	 * @param fromTime the from time
	 * @param toTime the to time
	 * @param startDateTime the start date time
	 * @return the list
	 */
	List<DowntimeLogs> findAllByFunctionalityInAndEndDateBetweenAndStartDateBefore(List<PnFunctionality> functionality,
			OffsetDateTime fromTime, OffsetDateTime toTime, OffsetDateTime startDateTime);
	
	/**
	 * Find all by functionality in and start date after.
	 *
	 * @param functionality the functionality
	 * @param fromTime the from time
	 * @return the list
	 */
	List<DowntimeLogs> findAllByFunctionalityInAndStartDateAfter(List<PnFunctionality> functionality,
			OffsetDateTime fromTime);

	/**
	 * Find all by functionality in and end date after and start date before.
	 *
	 * @param functionality the functionality
	 * @param fromTime the from time
	 * @param startDateTime the start date time
	 * @return the list
	 */
	List<DowntimeLogs> findAllByFunctionalityInAndEndDateAfterAndStartDateBefore(List<PnFunctionality> functionality,
			OffsetDateTime fromTime, OffsetDateTime startDateTime);
	
	/**
	 * Find by functionality and end date is null.
	 *
	 * @param functionality the functionality
	 * @return the downtime logs
	 */
	DowntimeLogs findByFunctionalityAndEndDateIsNull(PnFunctionality functionality);
	
	/**
	 * Query that recovers any downtime possibly lost from the sqs queue
	 * @return the list
	 */
	List<DowntimeLogs> findAllByEndDateIsNotNullAndLegalFactIdIsNull();
	
	DowntimeLogs findFirstByLegalFactId(String legalFactId);
}