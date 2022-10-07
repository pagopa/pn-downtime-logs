package it.pagopa.pn.downtime.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import it.pagopa.pn.downtime.mapper.DowntimeLogsMapper;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnDowntimeEntry;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnDowntimeHistoryResponse;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnStatusResponse;
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

	/** The dynamo DB mapper. Log */
	@Autowired
	@Qualifier("logMapper")
	private DynamoDBMapper dynamoDBMapperLog;

	/** The downtime logs mapper. */
	@Autowired
	DowntimeLogsMapper downtimeLogsMapper;

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

		Map<String, AttributeValue> eav1 = new HashMap<>();

		List<String> values = functionality.stream().map(fu -> fu.getValue()).collect(Collectors.toList());

		String expression = "";
		for (String s : values) {
			eav1.put(":functionality" + (values.indexOf(s) + 1), new AttributeValue().withS(s));
			expression = expression.concat(":functionality" + (values.indexOf(s) + 1) + ",");
		}

		eav1.put(":startDate1", new AttributeValue().withS(fromTime.toString()));

		if (toTime != null) {

			eav1.put(":endDate1", new AttributeValue().withS(toTime.toString()));
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
					.withFilterExpression("functionality in (" + expression.substring(0, expression.length() - 1)
							+ ") and  (startDate BETWEEN :startDate1 AND :endDate1 or  endDate BETWEEN :startDate1 AND :endDate1)")
					.withExpressionAttributeValues(eav1);

			listHistory = dynamoDBMapperLog.parallelScan(DowntimeLogs.class, scanExpression, 3);

		} else {
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
					.withFilterExpression("functionality in (" + expression.substring(0, expression.length() - 1)
							+ ") and  (startDate > :startDate1  or endDate > :startDate1 and startDate < :startDate1 )")
					.withExpressionAttributeValues(eav1);

			listHistory = dynamoDBMapperLog.parallelScan(DowntimeLogs.class, scanExpression, 3);

		}

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
			Map<String, AttributeValue> eav1 = new HashMap<>();
			eav1.put(":functionality1", new AttributeValue().withS(pn.getValue()));
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
					.withFilterExpression("functionality =:functionality1 and  attribute_not_exists(endDate) ")
					.withExpressionAttributeValues(eav1);

			List<DowntimeLogs> logs = dynamoDBMapperLog.parallelScan(DowntimeLogs.class, scanExpression, 3);

			if (logs != null && !logs.isEmpty() && PnFunctionalityStatus.KO.equals(logs.get(0).getStatus())) {
				PnDowntimeEntry incident = downtimeLogsMapper.downtimeLogsToPnDowntimeEntry(logs.get(0));
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
		downtimeLogs.setStartDate(startDate);
		downtimeLogs.setStatus(PnFunctionalityStatus.KO);
		downtimeLogs.setStartEventUuid(startEventUuid);
		downtimeLogs.setFunctionality(functionality);
		downtimeLogs.setUuid(uuid);
		downtimeLogs.setFileAvailable(false);
		dynamoDBMapperLog.save(downtimeLogs);
	}

	@Override
	public List<DowntimeLogs> findAllByEndDateIsNotNullAndLegalFactIdIsNull() {
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
				.withFilterExpression("attribute_not_exists(legalFactId) and  attribute_exists(endDate) ");

		return dynamoDBMapperLog.parallelScan(DowntimeLogs.class, scanExpression, 3);
	}
}
