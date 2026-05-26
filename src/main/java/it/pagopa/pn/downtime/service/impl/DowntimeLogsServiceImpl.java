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
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Service
@RequiredArgsConstructor
@CustomLog
public class DowntimeLogsServiceImpl implements DowntimeLogsService {

    @Autowired
    private DynamoDbTable<DowntimeLogs> downtimeLogsTable;

    @Autowired
    private DowntimeLogsMapper downtimeLogsMapper;

    @Value("${history.index}")
    private String historyIndex;

    @Value("${amazon.dynamodb.log.endpoint}")
    private String downtimeLogsTableName;

    @Override
    public PnDowntimeHistoryResponse getStatusHistory(OffsetDateTime fromTime, OffsetDateTime toTime,
                                                      List<PnFunctionality> functionality, String page, String size) {

        log.info("getStatusHistory - Input - fromTime: " + fromTime.toString() + " toTime: "
                + (toTime != null ? toTime.toString() : "") + " functionality: "
                + (functionality != null ? functionality.toString() : "") + " page: " + page + " size: " + size);

        List<DowntimeLogs> listHistoryResults = getStatusHistoryResults(fromTime, toTime, functionality, false);

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

    public List<DowntimeLogs> getStatusHistoryResults(OffsetDateTime fromTime, OffsetDateTime toTime,
                                                      List<PnFunctionality> functionality, boolean resolvedOnly) {

        List<DowntimeLogs> listHistory = new ArrayList<>();

        if (functionality == null || functionality.isEmpty()) {
            return listHistory;
        }

        Map<String, AttributeValue> attributes = new HashMap<>();

        List<String> values = functionality.stream().filter(Objects::nonNull).map(PnFunctionality::getValue).toList();

        String expression = "";
        for (String s : values) {
            attributes.put(":functionality" + (values.indexOf(s) + 1), AttributeValue.builder().s(s).build());
            expression = expression.concat(":functionality" + (values.indexOf(s) + 1) + ",");
        }
        attributes.put(":startDate1", AttributeValue.builder().s(fromTime.toString()).build());
        String filter = "functionality in (" + expression.substring(0, expression.length() - 1) + ")";
        if (toTime != null) {
            attributes.put(":endDate1", AttributeValue.builder().s(toTime.toString()).build());
            if (!resolvedOnly) {
                filter = filter.concat(
                        " and  (startDateAttribute BETWEEN :startDate1 AND :endDate1 or endDate BETWEEN :startDate1 AND :endDate1 or (startDateAttribute < :startDate1 and (endDate > :endDate1 or attribute_not_exists(endDate))))");
            } else {
                filter = filter.concat(" and (endDate BETWEEN :startDate1 AND :endDate1)");
            }
        } else {
            filter = filter.concat(
                    " and  (startDateAttribute > :startDate1 or endDate > :startDate1 or (startDateAttribute < :startDate1 and attribute_not_exists(endDate)))");
        }

        Expression filterExpression = Expression.builder()
                .expression(filter)
                .expressionValues(attributes)
                .build();

        QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue("downtimeHistory").build()))
                .filterExpression(filterExpression)
                .scanIndexForward(false)
                .build();

        log.info("Query expression filter={}", filter);

        DynamoDbIndex<DowntimeLogs> index = downtimeLogsTable.index(historyIndex);
        index.query(queryRequest).forEach(page -> page.items().forEach(listHistory::add));

        return listHistory;
    }

    @Override
    public PnStatusResponse currentStatus() {
        List<PnDowntimeEntry> openIncidents = new ArrayList<>();
        PnStatusResponse pnStatusResponseEntry = new PnStatusResponse();
        try {
            for (PnFunctionality pn : PnFunctionality.values()) {
                Expression scanFilter = Expression.builder()
                        .expression("functionality =:functionality1 and attribute_not_exists(endDate)")
                        .expressionValues(Map.of(":functionality1", AttributeValue.builder().s(pn.getValue()).build()))
                        .build();

                ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                        .filterExpression(scanFilter)
                        .build();

                List<DowntimeLogs> logs = downtimeLogsTable.scan(scanRequest).items().stream().toList();

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
        downtimeLogsTable.putItem(downtimeLogs);
        log.info("Inserted data in DynamoDB table {}", StringUtils.substringAfterLast(downtimeLogsTableName, "/"));
    }

    @Override
    public List<DowntimeLogs> findAllByEndDateIsNotNullAndLegalFactIdIsNull() {
        Expression scanFilter = Expression.builder()
                .expression("attribute_not_exists(legalFactId) and attribute_exists(endDate)")
                .build();

        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(scanFilter)
                .build();

        return downtimeLogsTable.scan(scanRequest).items().stream().toList();
    }

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
        List<DowntimeLogs> listHistoryResults = getStatusHistoryResults(fromTime, toTime, allFunctionalities, true);
        PnDowntimeHistoryResponse response = new PnDowntimeHistoryResponse();

        response.setResult(listHistoryResults != null ? listHistoryResults.stream()
                .filter(DowntimeLogs::getFileAvailable)
                .map(downtime -> downtimeLogsMapper.downtimeLogsToPnDowntimeEntry(downtime))
                .toList() : Collections.emptyList()
        );
        log.info("Resolved response={}", response);
        return response;
    }
}
