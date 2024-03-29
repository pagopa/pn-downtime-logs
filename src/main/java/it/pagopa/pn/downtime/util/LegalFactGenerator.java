package it.pagopa.pn.downtime.util;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LegalFactGenerator {

	public static final String FIELD_START_DATE = "startDate";
	public static final String FIELD_START_DATE_TIME = "timeReferenceStartDate";
	public static final String FIELD_END_DATE = "endDate";
	public static final String FIELD_END_DATE_TIME = "timeReferenceEndDate";
	private final DocumentComposition documentComposition;


	public LegalFactGenerator(
			DocumentComposition documentComposition) {
		this.documentComposition = documentComposition;

	}


	public byte[] generateLegalFact(DowntimeLogs downtime) throws IOException, TemplateException {
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		DateTimeFormatter fmtTime = DateTimeFormatter.ofPattern("HH:mm");
		log.info("generateLegalFact");
		Map<String, Object> templateModel = new HashMap<>();
		OffsetDateTime newStartDate = DowntimeLogUtil.getOffsetDateTimeFromGmtTime(downtime.getStartDate());
		OffsetDateTime newEndDate = DowntimeLogUtil.getOffsetDateTimeFromGmtTime(downtime.getEndDate());

		templateModel.put(FIELD_START_DATE	,  newStartDate.format(fmt));
		templateModel.put(FIELD_START_DATE_TIME	,  newStartDate.format(fmtTime));
		templateModel.put(FIELD_END_DATE	,  newEndDate.format(fmt));
		templateModel.put(FIELD_END_DATE_TIME	,  newEndDate.format(fmtTime));

		return documentComposition.executePdfTemplate(
				DocumentComposition.TemplateType.LEGAL_FACT,
				templateModel
				);

	}


}

