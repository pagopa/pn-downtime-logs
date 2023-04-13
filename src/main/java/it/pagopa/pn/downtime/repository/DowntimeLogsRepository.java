package it.pagopa.pn.downtime.repository;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionality;

@Component
public class DowntimeLogsRepository {

	@Autowired
	private DynamoDBMapper dynamoDBMapper;

	/**
	 * Retrieves an instance of DowntimeLogs open in the future relative to
	 * specified date, for specified date, for the specified functionality and
	 * event.
	 *
	 * @param date           an OffsetDateTime object representing the date to
	 *                       search for open DowntimeLogs in the future
	 * @param functionality  a PnFunctionality object representing the functionality
	 *                       to search for
	 * @param eventTimestamp an OffsetDateTime object representing the timestamp of
	 *                       the event to search for
	 * @return an Optional<DowntimeLogs> object representing the DowntimeLogs
	 *         instance found or an empty Optional if none was found
	 */
	public Optional<DowntimeLogs> findOpenDowntimeLogsFuture(OffsetDateTime date, PnFunctionality functionality,
			OffsetDateTime eventTimestamp) {
		DynamoDBQueryExpression<DowntimeLogs> queryExpression = buildDynamoDBQueryExpression(date, functionality,
				eventTimestamp)
				.withKeyConditionExpression(
						"functionalityStartYear =:functionalityStartYearInput and startDate >=:startDateInput")
				.withFilterExpression("functionality = :functionalityInput and (attribute_not_exists(endDate))");

		QueryResultPage<DowntimeLogs> queryResultPage = dynamoDBMapper.queryPage(DowntimeLogs.class, queryExpression);
		List<DowntimeLogs> result = queryResultPage != null ? queryResultPage.getResults() : List.of();

		return (result == null || result.isEmpty()) ? Optional.empty() : Optional.of(result.get(0));
	}

	/**
	 * Retrieves an instance of DowntimeLogs between the specified startDate and
	 * endDate.
	 *
	 * @param date           an OffsetDateTime object representing the date to
	 *                       search for open DowntimeLogs in the future
	 * @param functionality  a PnFunctionality object representing the functionality
	 *                       to search for
	 * @param eventTimestamp an OffsetDateTime object representing the timestamp of
	 *                       the event to search for
	 * @return an Optional<DowntimeLogs> object representing the DowntimeLogs
	 *         instance found or an empty Optional if none was found
	 */
	public Optional<DowntimeLogs> findDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists(OffsetDateTime date,
			PnFunctionality functionality, OffsetDateTime eventTimestamp) {
		DynamoDBQueryExpression<DowntimeLogs> queryExpression = buildDynamoDBQueryExpression(date, functionality,
				eventTimestamp)
				.withFilterExpression(
						"functionality = :functionalityInput and endDate > :startDateInput and attribute_exists(endDate)");

		QueryResultPage<DowntimeLogs> queryResultPage = dynamoDBMapper.queryPage(DowntimeLogs.class, queryExpression);
		List<DowntimeLogs> result = queryResultPage != null ? queryResultPage.getResults() : List.of();

		return (result == null || result.isEmpty()) ? Optional.empty() : Optional.of(result.get(0));
	}

	/**
	 * Retrieves an instance of DowntimeLogs occurred in the past.
	 *
	 * @param date           an OffsetDateTime object representing the date to
	 *                       search for open DowntimeLogs in the future
	 * @param functionality  a PnFunctionality object representing the functionality
	 *                       to search for
	 * @param eventTimestamp an OffsetDateTime object representing the timestamp of
	 *                       the event to search for
	 * @return an Optional<DowntimeLogs> object representing the DowntimeLogs
	 *         instance found or an empty Optional if none was found
	 */
	public Optional<DowntimeLogs> findLastDowntimeLogsWithoutEndDate(OffsetDateTime date, PnFunctionality functionality,
			OffsetDateTime eventTimestamp) {

		DynamoDBQueryExpression<DowntimeLogs> queryExpression = buildDynamoDBQueryExpression(date, functionality,
				eventTimestamp)
				.withFilterExpression("functionality =:functionalityInput and (attribute_not_exists(endDate))");

		QueryResultPage<DowntimeLogs> queryResultPage = dynamoDBMapper.queryPage(DowntimeLogs.class, queryExpression);
		List<DowntimeLogs> result = (queryResultPage != null && !CollectionUtils.isEmpty(queryResultPage.getResults())) ? queryResultPage.getResults()
				: findLastDowntimeLogs(date, functionality, eventTimestamp);

		return (result == null || result.isEmpty()) ? Optional.empty() : Optional.of(result.get(0));
	}

	public List<DowntimeLogs> findLastDowntimeLogs(OffsetDateTime date, PnFunctionality functionality,
			OffsetDateTime eventTimestamp) {
		DynamoDBQueryExpression<DowntimeLogs> queryExpression = buildDynamoDBQueryExpression(date, functionality,
				eventTimestamp).withFilterExpression("functionality =:functionalityInput");

		QueryResultPage<DowntimeLogs> queryResultPage = dynamoDBMapper.queryPage(DowntimeLogs.class, queryExpression);

		return queryResultPage != null ? queryResultPage.getResults() : List.of();
	}

	/**
	 * Builds the DynamoDBQueryExpression object for querying the DowntimeLogs
	 * table.
	 *
	 * @param date           an OffsetDateTime object representing the date to
	 *                       search for open DowntimeLogs in the future
	 * @param functionality  a PnFunctionality object representing the functionality
	 *                       to search for
	 * @param eventTimestamp an OffsetDateTime object representing the timestamp of
	 *                       the event to search for
	 * @return DynamoDBQueryExpression<DowntimeLogs> object
	 */
	private DynamoDBQueryExpression<DowntimeLogs> buildDynamoDBQueryExpression(OffsetDateTime date,
			PnFunctionality functionality, OffsetDateTime eventTimestamp) {
		Map<String, AttributeValue> eav = new HashMap<>();
		eav.put(":functionalityStartYearInput",
				new AttributeValue().withS(functionality.getValue().concat(date.toString().substring(0, 4))));
		eav.put(":functionalityInput", new AttributeValue().withS(functionality.getValue()));
		eav.put(":startDateInput", new AttributeValue().withS(eventTimestamp.toString()));

		return new DynamoDBQueryExpression<DowntimeLogs>()
				.withKeyConditionExpression(
						"functionalityStartYear =:functionalityStartYearInput and startDate <=:startDateInput")
				.withScanIndexForward(false).withExpressionAttributeValues(eav);
	}
}
