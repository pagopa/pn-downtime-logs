package it.pagopa.pn.downtime.service.impl;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.LegalFactGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionalityStatus;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.producer.DowntimeLogsSend;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
import it.pagopa.pn.downtime.service.DowntimeLogsService;
import it.pagopa.pn.downtime.service.EventService;
import it.pagopa.pn.downtime.util.Constants;
import it.pagopa.pn.downtime.util.DowntimeLogUtil;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@CustomLog
public class EventServiceImpl implements EventService {

    @Autowired
    private DowntimeLogsService downtimeLogsService;

    @Autowired
    private LegalFactGenerator legalFactGenerator;

    @Autowired
    private DowntimeLogsSend producer;

    @Value("${amazon.sqs.end-point.acts-queue}")
    private String url;

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    private DowntimeLogsRepository repository;

    @Value("${amazon.dynamodb.event.endpoint}")
    private String eventTableName;

    /**
     * Adds the status change event.
     *
     * @param xPagopaPnUid        the x pagopa pn uid. Required
     * @param pnStatusUpdateEvent the input for the new event. Required
     * @return the void
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void addStatusChangeEvent(String xPagopaPnUid, List<PnStatusUpdateEvent> pnStatusUpdateEvent)
            throws IOException {

        String errorMessages = "";

        for (PnStatusUpdateEvent event : pnStatusUpdateEvent) {

            OffsetDateTime date = DowntimeLogUtil.getGmtTimeFromOffsetDateTime(event.getTimestamp());

            PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();
            PnAuditLogEvent logEvent = auditLogBuilder.before(PnAuditLogEventType.AUD_NT_INSERT,
                            "addStatusChangeEvent - xPagopaPnUid={}, timestamp={}, functionality={}, status={}, sourceType={}, source={}, htmlDescription={}",
                            xPagopaPnUid, date, event.getFunctionality(), event.getStatus().getValue(), event.getSourceType(), event.getSource(), event.getHtmlDescription())
                    .mdcEntry("uid", xPagopaPnUid).build();

            logEvent.log();
            for (PnFunctionality functionality : event.getFunctionality()) {
                try {
                    DowntimeLogs dt = findDowntimeLogs(date, functionality, event);
                    createEvent(xPagopaPnUid, dt, functionality, event);
                    logEvent.generateSuccess().log();
                } catch (IllegalArgumentException e) {
                    errorMessages = errorMessages.concat(e.getMessage());
                    logEvent.generateFailure("Error creating event: " + errorMessages).log();
                }
            }
        }
        if (!errorMessages.isEmpty()) {
            throw new IllegalArgumentException(errorMessages);
        }
    }

    private Optional<DowntimeLogs> resultQuery(OffsetDateTime date, PnFunctionality functionality,
                                               PnStatusUpdateEvent event) {

        OffsetDateTime eventTimestamp = DowntimeLogUtil.getGmtTimeFromOffsetDateTime(event.getTimestamp());

        Optional<DowntimeLogs> queryResultDowntimeLogs;

        if (eventTimestamp.isBefore(DowntimeLogUtil.getOffsetDateTimeNowFormatted())) {
            if (event.getStatus().equals(PnFunctionalityStatus.KO)) {
                queryResultDowntimeLogs = repository.findOpenDowntimeLogsFuture(date, functionality, eventTimestamp);
                checkQueryResultAndThrowIfDowntimeExists(queryResultDowntimeLogs);
            }
            queryResultDowntimeLogs = repository.findDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists(date,
                    functionality, eventTimestamp);
            checkQueryResultAndThrowIfDowntimeExists(queryResultDowntimeLogs);
        }

        queryResultDowntimeLogs = repository.findLastDowntimeLogsWithoutEndDate(date, functionality, eventTimestamp);

        checkQueryResultNextDowntimeLogsWithStatusOK(queryResultDowntimeLogs, eventTimestamp, event);

        return queryResultDowntimeLogs;
    }

    public void checkQueryResultAndThrowIfDowntimeExists(Optional<DowntimeLogs> queryResultPageDowntimeLogs) {
        if (queryResultPageDowntimeLogs.isPresent()) {
            DowntimeLogs resultDowntimeLogs = queryResultPageDowntimeLogs.get();
            throw new IllegalArgumentException(String.format(Constants.GENERIC_CONFLICT_ERROR_ENGLISH_MESSAGE,
                    resultDowntimeLogs.getFunctionality(), resultDowntimeLogs.getStartDate(),
                    resultDowntimeLogs.getEndDate()));
        }
    }

    public void checkQueryResultNextDowntimeLogsWithStatusOK(Optional<DowntimeLogs> queryResultDowntimeLogs,
                                                             OffsetDateTime eventTimestamp, PnStatusUpdateEvent event) {
        if (queryResultDowntimeLogs.isPresent()) {
            Optional<DowntimeLogs> nextDowntimeLogs = repository.findNextDowntimeLogs(
                    queryResultDowntimeLogs.get().getStartDate(), queryResultDowntimeLogs.get().getFunctionality(),
                    queryResultDowntimeLogs.get().getStartDate());

            if (nextDowntimeLogs.isPresent() && !event.getStatus().equals(PnFunctionalityStatus.KO)
                    && eventTimestamp.isAfter(nextDowntimeLogs.get().getStartDate())) {
                throw new IllegalArgumentException(String.format(Constants.GENERIC_CONFLICT_ERROR_ENGLISH_MESSAGE,
                        nextDowntimeLogs.get().getFunctionality(), nextDowntimeLogs.get().getStartDate(),
                        nextDowntimeLogs.get().getEndDate()));
            }
        }
    }

    /**
     * Check if a new downtime has to be created.
     *
     * @param functionality the functionality
     * @param eventId       the id of the starting event
     * @param event         the input event
     * @param xPagopaPnUid  the x pagopa pn uid
     * @param dt            the previous downtime for the given functionality
     * @throws IOException
     */

    public void checkCreateDowntime(PnFunctionality functionality, String eventId, PnStatusUpdateEvent event,
                                    String xPagopaPnUid, DowntimeLogs dt) {

        if ((dt != null && event.getStatus().equals(PnFunctionalityStatus.KO) && dt.getEndDate() != null
                && dt.getEndDate().compareTo(DowntimeLogUtil.getGmtTimeFromOffsetDateTime(event.getTimestamp())) <= 0)
                || (dt == null && event.getStatus().equals(PnFunctionalityStatus.KO))) {

            downtimeLogsService.saveDowntimeLogs(
                    functionality.getValue().concat(event.getTimestamp().toString().substring(0, 4)),
                    event.getTimestamp(), functionality, eventId, xPagopaPnUid);
        }
    }

    /**
     * Check if the current downtime has to be closed and closes it.
     *
     * @param eventId the id of the closing event.
     * @param event   the input event
     * @param dt      the current downtime for the given functionality
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void checkUpdateDowntime(String eventId, PnStatusUpdateEvent event, DowntimeLogs dt) throws IOException {
        if (dt != null && !event.getStatus().equals(PnFunctionalityStatus.KO) && dt.getEndDate() == null) {

            OffsetDateTime newEndDate = DowntimeLogUtil.getGmtTimeFromOffsetDateTime(event.getTimestamp());

            dt.setEndDate(newEndDate);
            dt.setEndEventUuid(eventId);
            dt.setStatus(event.getStatus());
            dt.setHtmlDescription(event.getHtmlDescription());
            dynamoDBMapper.save(dt);
            producer.sendMessage(dt, url);
        }

    }

    /**
     * Creates the event and saves it and checks for the creation and update of the
     * current downtimes.
     *
     * @param xPagopaPnUid  the x pagopa pn uid
     * @param dt            the current downtime for the given functionality
     * @param functionality the functionality
     * @param event         the input event
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void createEvent(String xPagopaPnUid, DowntimeLogs dt, PnFunctionality functionality,
                            PnStatusUpdateEvent event) throws IOException {

        String saveUid = "";

        if (dt != null && event.getStatus().equals(PnFunctionalityStatus.OK) && dt.getEndDate() != null) {
            log.error(String.format(Constants.GENERIC_CREATING_EVENT_ERROR, functionality.getValue()));
        } else {
            saveUid = saveEvent(event.getTimestamp(), event.getTimestamp().toString().substring(0, 7), functionality,
                    event.getStatus(), event.getSourceType(), event.getSource(), xPagopaPnUid);
        }

        OffsetDateTime timestamp = DowntimeLogUtil.getGmtTimeFromOffsetDateTime(event.getTimestamp());

        if (timestamp.isBefore(DowntimeLogUtil.getOffsetDateTimeNowFormatted())) {
            checkCreateDowntime(functionality, saveUid, event, xPagopaPnUid, dt);
            checkUpdateDowntime(saveUid, event, dt);
        } else {
            throw new IOException(Constants.GENERIC_CREATING_FUTURE_EVENT_ERROR);
        }
    }

    /**
     * Creates and saves the new event.
     *
     * @param timestamp     the timestamp
     * @param yearMonth     the year month
     * @param functionality the functionality
     * @param status        the status
     * @param sourceType    the source type
     * @param source        the source
     * @return the string
     */
    public String saveEvent(OffsetDateTime timestamp, String yearMonth, PnFunctionality functionality,
                            PnFunctionalityStatus status, SourceTypeEnum sourceType, String source, String uuid) {
        Event event = new Event();
        OffsetDateTime newTimestamp = DowntimeLogUtil.getGmtTimeFromOffsetDateTime(timestamp);
        event.setTimestamp(newTimestamp);
        event.setYearMonth(yearMonth);
        event.setFunctionality(functionality);
        event.setStatus(status);
        event.setSourceType(sourceType);
        event.setSource(source);
        event.setUuid(uuid);
        log.debug("Inserting data {} in DynamoDB table {}", event.toString(),StringUtils.substringAfterLast(eventTableName, "/"));
        dynamoDBMapper.save(event);
        log.info("Inserted data in DynamoDB table {}",StringUtils.substringAfterLast(eventTableName, "/"));
        return event.getIdEvent();
    }

    @Override
    public byte[] previewLegalFact(PnStatusUpdateEvent event) throws IOException, NoSuchAlgorithmException, TemplateException {
        OffsetDateTime date = DowntimeLogUtil.getGmtTimeFromOffsetDateTime(event.getTimestamp());
        PnFunctionality functionality = event.getFunctionality().get(0);

        DowntimeLogs dt = findDowntimeLogs(date, functionality, event);
        if (dt == null) {
            throw new IllegalArgumentException(Constants.GENERIC_CONFLICT_ERROR_MESSAGE_TITLE);
        }
        OffsetDateTime endDate = DowntimeLogUtil.getGmtTimeFromOffsetDateTime(event.getTimestamp());
        dt.setEndDate(endDate);
        dt.setHtmlDescription(event.getHtmlDescription());

        return legalFactGenerator.generateMalfunctionLegalFact(dt);
    }

    private DowntimeLogs findDowntimeLogs(OffsetDateTime date, PnFunctionality functionality, PnStatusUpdateEvent event) {
        return resultQuery(date, functionality, event)
                .orElseGet(() -> resultQuery(date.minusYears(1), functionality, event).isPresent()
                        ? resultQuery(date.minusYears(1), functionality, event).get()
                        : null);
    }

}