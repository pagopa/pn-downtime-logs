package it.pagopa.pn.downtime.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.model.Event;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
import it.pagopa.pn.downtime.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

	@Autowired
	EventRepository eventRepository;
	@Autowired
	DowntimeLogsService downtimeLogsService;
	@Autowired
	LegalFactService legalFactService;
	@Autowired
	DowntimeLogsRepository downtimeLogsRepository;

	private DynamoDBMapper dynamoDBMapper;
	@Autowired
	private AmazonDynamoDB amazonDynamoDB;

	@Override
	public Void addStatusChangeEvent(String xPagopaPnUid, List<PnStatusUpdateEvent> pnStatusUpdateEvent)
			throws NoSuchAlgorithmException, IOException, TemplateException {
		log.info("addStatusChangeEvent");
		for (PnStatusUpdateEvent event : pnStatusUpdateEvent) {
			for (PnFunctionality functionality : event.getFunctionality()) {
				Map<String, AttributeValue> eav = new HashMap<>();
				eav.put(":startYear1", new AttributeValue()
						.withS(functionality.getValue().concat(event.getTimestamp().toString().substring(0, 4))));
				eav.put(":functionality1", new AttributeValue().withS(functionality.getValue()));
				eav.put(":startDate1", new AttributeValue().withS(event.getTimestamp().toString()));

				dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
				DynamoDBQueryExpression<DowntimeLogs> queryExpression = new DynamoDBQueryExpression<DowntimeLogs>()
						.withKeyConditionExpression("functionalityStartYear =:startYear1 and startDate <=:startDate1")
						.withFilterExpression("functionality = :functionality1").withScanIndexForward(false)
						.withExpressionAttributeValues(eav).withLimit(1);

				QueryResultPage<DowntimeLogs> downtimeLogs = dynamoDBMapper.queryPage(DowntimeLogs.class,
						queryExpression);
				DowntimeLogs dt = null;
				if (!downtimeLogs.getResults().isEmpty()) {
					dt = downtimeLogs.getResults().get(0);
				}
				createEvent(xPagopaPnUid, dt, functionality, event);
			}
		}
		return null;
	}

	public void saveDowntime(PnFunctionality functionality, String eventId, PnStatusUpdateEvent pnStatusUpdateEvent,
			String xPagopaPnUid) {
		downtimeLogsService.saveDowntimeLogs(
				functionality.getValue().concat(pnStatusUpdateEvent.getTimestamp().toString().substring(0, 4)),
				pnStatusUpdateEvent.getTimestamp(), functionality, pnStatusUpdateEvent.getStatus(), eventId,
				xPagopaPnUid);
	}

	public void checkCreateDowntime(PnFunctionality functionality, String eventId, PnStatusUpdateEvent event,
			String xPagopaPnUid, DowntimeLogs dt) {
		if ((dt != null && ((event.getStatus().equals(PnFunctionalityStatus.KO)
				&& (!dt.getStatus().equals(PnFunctionalityStatus.KO) || (dt.getStatus().equals(PnFunctionalityStatus.KO)
						&& dt.getEndDate() != null && dt.getEndDate().compareTo(event.getTimestamp()) <= 0)))
				|| (!event.getStatus().equals(PnFunctionalityStatus.KO)
						&& dt.getStatus().equals(PnFunctionalityStatus.KO) && dt.getEndDate() == null)))
				|| dt == null) {
			saveDowntime(functionality, eventId, event, xPagopaPnUid);
		}

	}

	public void checkUpdateDowntime(String eventId, PnStatusUpdateEvent event, DowntimeLogs dt)
			throws NoSuchAlgorithmException, IOException, TemplateException {

		if (dt != null && ((event.getStatus().equals(PnFunctionalityStatus.KO)
				&& !dt.getStatus().equals(PnFunctionalityStatus.KO))
				|| (!event.getStatus().equals(PnFunctionalityStatus.KO)
						&& dt.getStatus().equals(PnFunctionalityStatus.KO)))
				&& dt.getEndDate() == null) {
			if (!event.getStatus().equals(PnFunctionalityStatus.KO) && dt.getStatus().equals(PnFunctionalityStatus.KO)
					&& dt.getEndDate() == null) {

				dt.setEndDate(event.getTimestamp());
				legalFactService.generateLegalFact(dt);

			}
			if (dt.getEndDate() == null) {
				dt.setEndDate(event.getTimestamp());
			}
			dt.setEndEventUuid(eventId);
			downtimeLogsRepository.save(dt);
		}
	}

	public void createEvent(String xPagopaPnUid, DowntimeLogs dt, PnFunctionality functionality,
			PnStatusUpdateEvent event) throws NoSuchAlgorithmException, IOException, TemplateException {
		String saveUid = "";
		if (dt != null && event.getStatus().equals(PnFunctionalityStatus.OK)
				&& dt.getStatus().equals(PnFunctionalityStatus.KO) && dt.getEndDate() != null) {
			log.error("Errore nella creazione dell'evento");
		} else {
			saveUid = saveEvent(event.getTimestamp(), event.getTimestamp().toString().substring(0, 7), functionality,
					event.getStatus(), event.getSourceType(), event.getSource());
		}

		checkCreateDowntime(functionality, saveUid, event, xPagopaPnUid, dt);
		checkUpdateDowntime(saveUid, event, dt);

	}

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
