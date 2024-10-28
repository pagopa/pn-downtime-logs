package it.pagopa.pn.downtime.service.impl;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnDowntimeEntry;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnDowntimeHistoryResponse;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionalityStatus;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusResponse;
import it.pagopa.pn.downtime.mapper.DowntimeLogsMapper;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.service.DowntimeLogsService;
import it.pagopa.pn.downtime.util.DowntimeLogUtil;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@CustomLog
public class DowntimeLogsServiceImpl implements DowntimeLogsService {

	@Autowired
	private DynamoDBMapper dynamoDBMapper;

	@Autowired
	private DowntimeLogsMapper downtimeLogsMapper;

	@Value("${history.index}")
	private String historyIndex;

	@Value("${amazon.dynamodb.log.endpoint}")
	private String downtimeLogsTableName;
	/**
	 * Gets the status history.
	 *
	 * @param fromTime      starting timestamp of the research. Required
	 * @param toTime        ending timestamp of the research
	 * @param functionality functionalities for which the research has to be done
	 * @param page          the page of the research
	 * @param size          the size of the researcj
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
	 * @param fromTime      starting timestamp of the research. Required
	 * @param toTime        ending timestamp of the research
	 * @param functionality functionalities for which the research has to be done
	 * @return the combined results of the queries
	 */
	public List<DowntimeLogs> getStatusHistoryResults(OffsetDateTime fromTime, OffsetDateTime toTime,
			List<PnFunctionality> functionality) {

		List<DowntimeLogs> listHistory = new ArrayList<>();

		if (functionality == null || functionality.isEmpty()) {
			return listHistory;
		}

		Map<String, AttributeValue> attributes = new HashMap<>();

		List<String> values = functionality.stream().filter(Objects::nonNull).map(PnFunctionality::getValue).toList();

		String expression = "";
		for (String s : values) {
			attributes.put(":functionality" + (values.indexOf(s) + 1), new AttributeValue().withS(s));
			expression = expression.concat(":functionality" + (values.indexOf(s) + 1) + ",");
		}
		attributes.put(":history1", new AttributeValue().withS("downtimeHistory"));
		attributes.put(":startDate1", new AttributeValue().withS(fromTime.toString()));
		String filter = "functionality in (" + expression.substring(0, expression.length() - 1) + ")";
		if (toTime != null) {
			attributes.put(":endDate1", new AttributeValue().withS(toTime.toString()));
			filter = filter.concat(
					" and  (startDateAttribute BETWEEN :startDate1 AND :endDate1 or endDate BETWEEN :startDate1 AND :endDate1 or (startDateAttribute < :startDate1 and (endDate > :endDate1 or attribute_not_exists(endDate))))");
		} else {
			filter = filter.concat(
					" and  (startDateAttribute > :startDate1 or endDate > :startDate1 or (startDateAttribute < :startDate1 and attribute_not_exists(endDate)))");

		}

		DynamoDBQueryExpression<DowntimeLogs> queryExpression = new DynamoDBQueryExpression<DowntimeLogs>()
				.withIndexName(historyIndex).withKeyConditionExpression("history =:history1")
				.withFilterExpression(filter).withScanIndexForward(false).withConsistentRead(false)
				.withExpressionAttributeValues(attributes);
		log.info("Query expression{}", queryExpression.toString());

		listHistory = dynamoDBMapper.query(DowntimeLogs.class, queryExpression);

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
		try {
			for (PnFunctionality pn : PnFunctionality.values()) {
				Map<String, AttributeValue> eav1 = new HashMap<>();
				eav1.put(":functionality1", new AttributeValue().withS(pn.getValue()));
				DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
						.withFilterExpression("functionality =:functionality1 and  attribute_not_exists(endDate) ")
						.withExpressionAttributeValues(eav1);

				List<DowntimeLogs> logs = dynamoDBMapper.parallelScan(DowntimeLogs.class, scanExpression, 3);

				if (logs != null && !logs.isEmpty() && PnFunctionalityStatus.KO.equals(logs.get(0).getStatus())) {
					PnDowntimeEntry incident = downtimeLogsMapper.downtimeLogsToPnDowntimeEntry(logs.get(0));
					openIncidents.add(incident);
				}
			}
			pnStatusResponseEntry.setFunctionalities(Arrays.asList(PnFunctionality.values()));
			pnStatusResponseEntry.setOpenIncidents(openIncidents);
			pnStatusResponseEntry.setStatus(HttpStatus.OK.value());
			pnStatusResponseEntry.setTitle(HttpStatus.OK.name());
			pnStatusResponseEntry.setDetail(HttpStatus.OK.name());
            log.info("Response: " + pnStatusResponseEntry.toString());
		} catch (Exception e) {
			log.error("Error occurred while fetching current status: ", e);
			pnStatusResponseEntry.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			pnStatusResponseEntry.setTitle(PnFunctionalityStatus.KO.name());
			pnStatusResponseEntry.setDetail(PnFunctionalityStatus.KO.name());
		}
		return pnStatusResponseEntry;
	}

	/**
	 * Save a new downtime logs.
	 *
	 * @param functionalityStartYear the functionality start year which is a
	 *                               comination of functionality and the yeat of the
	 *                               startDate
	 * @param startDate              the start date
	 * @param functionality          the functionality
	 * @param startEventUuid         the uuid of start event
	 * @param uuid                   the uuid
	 */
	@Override
	public void saveDowntimeLogs(String functionalityStartYear, OffsetDateTime startDate, PnFunctionality functionality,
			String startEventUuid, String uuid) {
		
		DowntimeLogs downtimeLogs = new DowntimeLogs();
		downtimeLogs.setFunctionalityStartYear(functionalityStartYear);
		OffsetDateTime newStartDate = DowntimeLogUtil.getGmtTimeFromOffsetDateTime(startDate);
		downtimeLogs.setStartDate(newStartDate);
		downtimeLogs.setStartDateAttribute(newStartDate);
		downtimeLogs.setStatus(PnFunctionalityStatus.KO);
		downtimeLogs.setStartEventUuid(startEventUuid);
		downtimeLogs.setFunctionality(functionality);
		downtimeLogs.setUuid(uuid);
		downtimeLogs.setFileAvailable(false);
		downtimeLogs.setHistory("downtimeHistory");
		log.debug("Inserting data {} in DynamoDB table {}", downtimeLogs.toString(), StringUtils.substringAfterLast(downtimeLogsTableName, "/"));
		dynamoDBMapper.save(downtimeLogs);
		log.info("Inserted data in DynamoDB table {}", StringUtils.substringAfterLast(downtimeLogsTableName, "/"));
	}

	@Override
	public List<DowntimeLogs> findAllByEndDateIsNotNullAndLegalFactIdIsNull() {
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
				.withFilterExpression("attribute_not_exists(legalFactId) and  attribute_exists(endDate) ");

		return dynamoDBMapper.parallelScan(DowntimeLogs.class, scanExpression, 3);
	}

	/**
	 * Gets the resolved history.
	 *
	 * @param year      year of the research.
	 * @param month        month of the research
	 * @return all the downtimes present in the period of time specified
	 */
	@Override
	public PnDowntimeHistoryResponse getResolved(Integer year, Integer month) {
		OffsetDateTime currentDate = OffsetDateTime.now(ZoneOffset.UTC);

		if (year == null) { year = currentDate.getYear(); }
		if (month == null) { month = currentDate.getMonthValue(); }

		YearMonth yearMonth = YearMonth.of(year, month);

		OffsetDateTime fromTime = yearMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
		OffsetDateTime toTime = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneOffset.UTC).toOffsetDateTime();
		log.info("Get status history fromTime={}, toTime={}", fromTime, toTime);
		List<PnFunctionality> allFunctionalities = List.of(PnFunctionality.NOTIFICATION_CREATE,
				PnFunctionality.NOTIFICATION_WORKFLOW,
				PnFunctionality.NOTIFICATION_VISUALIZATION
		);
		List<DowntimeLogs> listHistoryResults = getStatusHistoryResults(fromTime, toTime, allFunctionalities);
		PnDowntimeHistoryResponse response = new PnDowntimeHistoryResponse();

		response.setResult( listHistoryResults != null ? listHistoryResults.stream()
				.filter( DowntimeLogs::getFileAvailable )
				.map( downtime -> downtimeLogsMapper.downtimeLogsToPnDowntimeEntry(downtime) )
				.toList() : Collections.emptyList()
		);
		log.info("Resolved response={}", response);
		return response;
	}
}
