package it.pagopa.pn.downtime.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.producer.DowntimeLogsSend;
import it.pagopa.pn.downtime.service.DowntimeLogsService;
import lombok.CustomLog;

@Component
@CustomLog
public class LegalFactIdJob implements Job {

	private static final String JOB_LOG_NAME = "Recover LegalFactId Job ";

	/** The downtime logs service. */
	@Autowired
	private DowntimeLogsService downtimeLogsService;
	/** The producer. */
	@Autowired
	DowntimeLogsSend producer;
	/** The url for the generate legal fact queue */
	@Value("${amazon.sqs.end-point.acts-queue}")
	private String url;

	public void execute(JobExecutionContext context) {
		log.info(JOB_LOG_NAME + "started");
		Instant start = Instant.now();
		List<DowntimeLogs> downtimeList = downtimeLogsService.findAllByEndDateIsNotNullAndLegalFactIdIsNull();
		downtimeList.stream().forEach(downtime -> {
			try {
				producer.sendMessage(downtime, url);
			} catch (JsonProcessingException e) {
				log.error(e.getMessage());
			}
		});
		Instant end = Instant.now();
		log.info(JOB_LOG_NAME + "ended in " + Duration.between(start, end).getSeconds() + " seconds");
	}

}