package it.pagopa.pn.downtime.service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.producer.DowntimeLogsSend;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
import it.pagopa.pn.downtime.repository.EventRepository;
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

	/** The event repository. */
	@Autowired
	EventRepository eventRepository;

	/** The downtime logs service. */
	@Autowired
	DowntimeLogsService downtimeLogsService;

	/** The downtime logs repository. */
	@Autowired
	DowntimeLogsRepository downtimeLogsRepository;

	/** The producer. */
	@Autowired
	DowntimeLogsSend producer;

	/** The url for the generate legal fact queue */
	@Value("${amazon.sqs.end-point.acts-queue}")
	private String url;

	/** The dynamo DB mapper. */
	@Autowired
	private DynamoDBMapper dynamoDBMapper;

	/**
	 * Adds the status change event.
	 *
	 * @param xPagopaPnUid        the x pagopa pn uid. Required
	 * @param pnStatusUpdateEvent the input for the new event. Required
	 * @return the void
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public Void addStatusChangeEvent(String xPagopaPnUid, List<PnStatusUpdateEvent> pnStatusUpdateEvent)
			throws IOException {
		log.info("addStatusChangeEvent");
		for (PnStatusUpdateEvent event : pnStatusUpdateEvent) {
			log.info("Input: " + event.toString());
			for (PnFunctionality functionality : event.getFunctionality()) {

				OffsetDateTime date = event.getTimestamp();
				QueryResultPage<DowntimeLogs> downtimeLogs = resultQuery(date, functionality, event);

				DowntimeLogs dt = null;

				if (downtimeLogs != null) {
					if (downtimeLogs.getResults() != null && !downtimeLogs.getResults().isEmpty()) {
						dt = downtimeLogs.getResults().get(0);
					} else {
						date = event.getTimestamp().minusYears(1);
						downtimeLogs = resultQuery(date, functionality, event);
						if (!downtimeLogs.getResults().isEmpty()) {
							dt = downtimeLogs.getResults().get(0);
						}
					}
				}
				createEvent(xPagopaPnUid, dt, functionality, event);
			}
		}
		return null;
	}

	private QueryResultPage<DowntimeLogs> resultQuery(OffsetDateTime date, PnFunctionality functionality,
			PnStatusUpdateEvent event) {
		Map<String, AttributeValue> eav1 = new HashMap<>();
		eav1.put(":startYear1",
				new AttributeValue().withS(functionality.getValue().concat(date.toString().substring(0, 4))));
		eav1.put(":functionality1", new AttributeValue().withS(functionality.getValue()));
		eav1.put(":startDate1", new AttributeValue().withS(event.getTimestamp().toString()));

		DynamoDBQueryExpression<DowntimeLogs> queryExpression = new DynamoDBQueryExpression<DowntimeLogs>()
				.withKeyConditionExpression("functionalityStartYear =:startYear1 and startDate <=:startDate1")
				.withFilterExpression("functionality = :functionality1").withScanIndexForward(false)
				.withExpressionAttributeValues(eav1).withLimit(1);

		return dynamoDBMapper.queryPage(DowntimeLogs.class, queryExpression);
	}

	/**
	 * Check if a new downtime has to be created.
	 *
	 * @param functionality the functionality
	 * @param eventId       the id of the starting event
	 * @param event         the input event
	 * @param xPagopaPnUid  the x pagopa pn uid
	 * @param dt            the previous downtime for the given functionality
	 */
	public void checkCreateDowntime(PnFunctionality functionality, String eventId, PnStatusUpdateEvent event,
			String xPagopaPnUid, DowntimeLogs dt) {
		if ((dt != null && event.getStatus().equals(PnFunctionalityStatus.KO) && dt.getEndDate() != null
				&& dt.getEndDate().compareTo(event.getTimestamp()) <= 0)
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

			dt.setEndDate(event.getTimestamp());
			dt.setEndEventUuid(eventId);
			downtimeLogsRepository.save(dt);
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
			log.error("Error creating event!");
		} else {
			saveUid = saveEvent(event.getTimestamp(), event.getTimestamp().toString().substring(0, 7), functionality,
					event.getStatus(), event.getSourceType(), event.getSource());
		}

		checkCreateDowntime(functionality, saveUid, event, xPagopaPnUid, dt);
		checkUpdateDowntime(saveUid, event, dt);

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
			PnFunctionalityStatus status, SourceTypeEnum sourceType, String source) {
		log.info("addStatusChangeEvent");
		Event event = new Event();
		event.setTimestamp(timestamp);
		event.setYearMonth(yearMonth);
		event.setFunctionality(functionality);
		event.setStatus(status);
		event.setSourceType(sourceType);
		event.setSource(source);
		eventRepository.save(event);
		return event.getUuid();
	}

}
