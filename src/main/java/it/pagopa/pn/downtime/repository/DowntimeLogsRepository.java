package it.pagopa.pn.downtime.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;


@Component
public class DowntimeLogsRepository {

	@Autowired
	private DynamoDbTable<DowntimeLogs> downtimeLogsTable;

	public Optional<DowntimeLogs> findOpenDowntimeLogsFuture(OffsetDateTime date, PnFunctionality functionality,
			OffsetDateTime eventTimestamp) {
		QueryEnhancedRequest req = buildQueryRequest(date, functionality, eventTimestamp,
				"functionalityStartYear =:functionalityStartYearInput and startDate >=:startDateInput",
				"functionality = :functionalityInput and attribute_not_exists(endDate)",
				false);

		List<DowntimeLogs> result = downtimeLogsTable.query(req).items().stream().toList();
		return (result == null || result.isEmpty()) ? Optional.empty() : Optional.of(result.get(0));
	}

	public Optional<DowntimeLogs> findDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists(OffsetDateTime date,
			PnFunctionality functionality, OffsetDateTime eventTimestamp) {
		QueryEnhancedRequest req = buildQueryRequest(date, functionality, eventTimestamp,
				"functionalityStartYear =:functionalityStartYearInput and startDate <:startDateInput",
				"functionality = :functionalityInput and endDate > :startDateInput and attribute_exists(endDate)",
				false);

		List<DowntimeLogs> result = downtimeLogsTable.query(req).items().stream().toList();
		return (result == null || result.isEmpty()) ? Optional.empty() : Optional.of(result.get(0));
	}

	public Optional<DowntimeLogs> findLastDowntimeLogsWithoutEndDate(OffsetDateTime date, PnFunctionality functionality,
			OffsetDateTime eventTimestamp) {
		QueryEnhancedRequest req = buildQueryRequest(date, functionality, eventTimestamp,
				"functionalityStartYear =:functionalityStartYearInput and startDate <:startDateInput",
				"functionality =:functionalityInput and attribute_not_exists(endDate)",
				false);

		List<DowntimeLogs> result = downtimeLogsTable.query(req).items().stream().toList();
		if (result != null && !result.isEmpty()) {
			return Optional.of(result.get(0));
		}
		List<DowntimeLogs> fallback = findLastDowntimeLogs(date, functionality, eventTimestamp);
		return (fallback == null || fallback.isEmpty()) ? Optional.empty() : Optional.of(fallback.get(0));
	}

	public List<DowntimeLogs> findLastDowntimeLogs(OffsetDateTime date, PnFunctionality functionality,
			OffsetDateTime eventTimestamp) {
		QueryEnhancedRequest req = buildQueryRequest(date, functionality, eventTimestamp,
				"functionalityStartYear =:functionalityStartYearInput and startDate <:startDateInput",
				"functionality =:functionalityInput",
				false);

		return downtimeLogsTable.query(req).items().stream().limit(1).toList();
	}

	public Optional<DowntimeLogs> findNextDowntimeLogs(OffsetDateTime date, PnFunctionality functionality,
			OffsetDateTime eventTimestamp) {
		QueryEnhancedRequest req = buildQueryRequest(date, functionality, eventTimestamp,
				"functionalityStartYear =:functionalityStartYearInput and startDate >:startDateInput",
				"functionality = :functionalityInput",
				true);

		List<DowntimeLogs> result = downtimeLogsTable.query(req).items().stream().toList();
		return (result == null || result.isEmpty()) ? Optional.empty() : Optional.of(result.get(0));
	}

	private QueryEnhancedRequest buildQueryRequest(OffsetDateTime date, PnFunctionality functionality,
			OffsetDateTime eventTimestamp, String keyCondition, String filterExpr, boolean scanIndexForward) {

		String pkValue = functionality.getValue().concat(date.toString().substring(0, 4));
		String skValue = eventTimestamp.toString();
		String funcValue = functionality.getValue();

		Map<String, AttributeValue> exprValues = Map.of(
				":functionalityStartYearInput", AttributeValue.builder().s(pkValue).build(),
				":startDateInput", AttributeValue.builder().s(skValue).build(),
				":functionalityInput", AttributeValue.builder().s(funcValue).build()
		);

		// Parse the key condition to build the appropriate QueryConditional
		QueryConditional keyConditional;
		if (keyCondition.contains(">=")) {
			keyConditional = QueryConditional.sortGreaterThanOrEqualTo(
					Key.builder().partitionValue(pkValue).sortValue(skValue).build());
		} else if (keyCondition.contains(">")) {
			keyConditional = QueryConditional.sortGreaterThan(
					Key.builder().partitionValue(pkValue).sortValue(skValue).build());
		} else if (keyCondition.contains("<=")) {
			keyConditional = QueryConditional.sortLessThanOrEqualTo(
					Key.builder().partitionValue(pkValue).sortValue(skValue).build());
		} else if (keyCondition.contains("<")) {
			keyConditional = QueryConditional.sortLessThan(
					Key.builder().partitionValue(pkValue).sortValue(skValue).build());
		} else {
			keyConditional = QueryConditional.keyEqualTo(
					Key.builder().partitionValue(pkValue).build());
		}

		Expression filter = Expression.builder()
				.expression(filterExpr)
				.expressionValues(exprValues)
				.build();

		return QueryEnhancedRequest.builder()
				.queryConditional(keyConditional)
				.filterExpression(filter)
				.scanIndexForward(scanIndexForward)
				.build();
	}
}
