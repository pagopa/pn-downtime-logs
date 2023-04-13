package it.pagopa.pn.downtime.service.impl;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.producer.DowntimeLogsSend;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
import it.pagopa.pn.downtime.service.DowntimeLogsService;
import it.pagopa.pn.downtime.service.EventService;
import it.pagopa.pn.downtime.util.Constants;
import it.pagopa.pn.downtime.util.DowntimeLogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class EventServiceImpl.
 */
@Service

/**
 * Instantiates a new event service impl.
 */
@RequiredArgsConstructor

/** The Constant log. */
@Slf4j
public class EventServiceImpl implements EventService {

	/** The downtime logs service. */
	@Autowired
	DowntimeLogsService downtimeLogsService;

	/** The producer. */
	@Autowired
	DowntimeLogsSend producer;

	/** The url for the generate legal fact queue */
	@Value("${amazon.sqs.end-point.acts-queue}")
	private String url;

	/** The dynamo DB mapper. */
	@Autowired
	private DynamoDBMapper dynamoDBMapper;

	@Autowired
	DowntimeLogsRepository repository;

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

			for (PnFunctionality functionality : event.getFunctionality()) {

				OffsetDateTime date = DowntimeLogUtil.getGmtTimeFromOffsetDateTime(event.getTimestamp());

				try {
					DowntimeLogs dt = resultQuery(date, functionality, event)
							.orElseGet(() -> resultQuery(date.minusYears(1), functionality, event).isPresent()
									? resultQuery(date.minusYears(1), functionality, event).get()
									: null);
					createEvent(xPagopaPnUid, dt, functionality, event);
				} catch (IllegalArgumentException e) {
					errorMessages = errorMessages.concat(e.getMessage());
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
		if (eventTimestamp.isBefore(DowntimeLogUtil.getGmtTimeNowFromOffsetDateTime())) {
			queryResultDowntimeLogs = repository.findOpenDowntimeLogsFuture(date, functionality, eventTimestamp);
			checkQueryResultAndThrowIfDowntimeExists(queryResultDowntimeLogs);

			queryResultDowntimeLogs = repository.findDowntimeLogsBetweenStartDateAndEndDateAndEndDateExists(date,
					functionality, eventTimestamp);
			checkQueryResultAndThrowIfDowntimeExists(queryResultDowntimeLogs);
		}

		queryResultDowntimeLogs = repository.findLastDowntimeLogsWithoutEndDate(date, functionality, eventTimestamp);

		if (queryResultDowntimeLogs.isPresent() && !event.getStatus().equals(PnFunctionalityStatus.KO)
				&& queryResultDowntimeLogs.get().getEndDate() == null) {
			Optional<DowntimeLogs> nextDowntimeLogs = repository.findNextDowntimeLogs(
					queryResultDowntimeLogs.get().getStartDate(), functionality,
					queryResultDowntimeLogs.get().getStartDate());
			if (nextDowntimeLogs.isPresent() && !eventTimestamp.isBefore(nextDowntimeLogs.get().getStartDate())) {
				throw new IllegalArgumentException(String.format(Constants.GENERIC_CONFLICT_ERROR_ENGLISH_MESSAGE,
						nextDowntimeLogs.get().getFunctionality(), nextDowntimeLogs.get().getStartDate(),
						nextDowntimeLogs.get().getEndDate()));
			}
		}
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

		if (timestamp.isBefore(DowntimeLogUtil.getGmtTimeNowFromOffsetDateTime())) {
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
		log.info("addStatusChangeEvent");
		Event event = new Event();
		OffsetDateTime newTimestamp = DowntimeLogUtil.getGmtTimeFromOffsetDateTime(timestamp);
		event.setTimestamp(newTimestamp);
		event.setYearMonth(yearMonth);
		event.setFunctionality(functionality);
		event.setStatus(status);
		event.setSourceType(sourceType);
		event.setSource(source);
		event.setUuid(uuid);
		dynamoDBMapper.save(event);
		return event.getIdEvent();
	}

}