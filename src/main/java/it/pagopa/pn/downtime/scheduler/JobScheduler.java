package it.pagopa.pn.downtime.scheduler;

import java.util.TimeZone;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JobScheduler {

	private static final String RETRY_JOB_GROUP = "retry-legalfactid";
	private final Scheduler scheduler;

	@Value("${scheduler.legalfactid.cron-expression}")
	private String cronExpressionNotify;
	@Value("${scheduler.legalfactid.active}")
	private boolean isActiveNotifyJob;

	public JobScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	public void startLegalFactIdJob() throws SchedulerException {
		if (isActiveNotifyJob)
			scheduleRecoverLegalFactIdJob();
	}

	public void scheduleRecoverLegalFactIdJob() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("check-payments-to-send", RETRY_JOB_GROUP);
		scheduleJob(jobKey, cronExpressionNotify, LegalFactIdJob.class);
	}

	private void scheduleJob(JobKey jobKey, String cronExpression, Class<? extends Job> jobClass)
			throws SchedulerException {
		for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
			scheduler.unscheduleJob(trigger.getKey());
		}

		JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobKey).build();

		Trigger trigger = TriggerBuilder.newTrigger().withSchedule(
				CronScheduleBuilder.cronSchedule(cronExpression).inTimeZone(TimeZone.getTimeZone("Europe/Rome")))
				.build();

		scheduler.scheduleJob(job, trigger);
	}

}
