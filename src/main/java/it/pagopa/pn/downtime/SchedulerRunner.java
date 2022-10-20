package it.pagopa.pn.downtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import it.pagopa.pn.downtime.scheduler.JobScheduler;


@Component
public class SchedulerRunner implements ApplicationRunner {

    private final JobScheduler jobScheduler;

    @Autowired
    public SchedulerRunner(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
       jobScheduler.startLegalFactIdJob();
    }
}