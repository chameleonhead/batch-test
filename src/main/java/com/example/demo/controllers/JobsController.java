package com.example.demo.controllers;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "jobs", description = "Jobs API")
@RestController
@RequestMapping("jobs")
public class JobsController {

    private final Job job;
    private final JobLauncher jobLauncher;

    public JobsController(Job job, @Qualifier("batchJobLauncher") JobLauncher jobLauncher) {
        this.job = job;
        this.jobLauncher = jobLauncher;
    }

    @PostMapping("test-job")
    public void run() throws Exception {
        jobLauncher.run(job, new JobParameters());
    }
}
