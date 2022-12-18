package com.example.demo.controllers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "jobs", description = "Jobs API")
@RestController
@RequestMapping("jobs")
public class JobsController {

    private final JobRegistry jobRegistry;
    private final JobLauncher jobLauncher;
    private final JobExplorer jobRepository;

    public JobsController(
            JobRegistry jobRegistry,
            @Qualifier("batchJobLauncher") JobLauncher jobLauncher,
            JobExplorer jobRepository) {
        this.jobRegistry = jobRegistry;
        this.jobLauncher = jobLauncher;
        this.jobRepository = jobRepository;
    }

    @PostMapping
    public JobStatus run() throws Exception {
        Job job = jobRegistry.getJob("test-job");
        HashMap<String, JobParameter> params = new HashMap<>();
        params.put("requestId", new JobParameter(UUID.randomUUID().toString()));
        return JobStatus.fromExecution(jobLauncher.run(job, new JobParameters(params)));
    }

    @GetMapping
    public List<JobStatus> index() {
        List<JobInstance> jobInstances = jobRepository.findJobInstancesByJobName("test-job", 0, 10);
        return jobInstances
                .stream()
                .map(i -> jobRepository.getLastJobExecution(i))
                .map(e -> JobStatus.fromExecution(e))
                .collect(Collectors.toList());
    }

    @GetMapping("{jobInstanceId}")
    public JobStatus get(@PathVariable("jobInstanceId") Long jobInstanceId) {
        JobInstance jobInstance = jobRepository.getJobInstance(jobInstanceId);
        return JobStatus.fromExecution(jobRepository.getLastJobExecution(jobInstance));
    }

    public static class JobStatus {
        private final Long jobInstanceId;
        private final String jobName;
        private final BatchStatus status;
        private final Date startTime;
        private final Date createTime;
        private final Date endTime;
        private final Date lastUpdated;
        private final ExitStatus exitStatus;

        public JobStatus(
                Long jobInstanceId,
                String jobName,
                BatchStatus status,
                Date startTime,
                Date endTime,
                Date createTime,
                Date lastUpdated,
                ExitStatus exitStatus) {
            this.jobInstanceId = jobInstanceId;
            this.jobName = jobName;
            this.status = status;
            this.startTime = startTime;
            this.createTime = createTime;
            this.endTime = endTime;
            this.lastUpdated = lastUpdated;
            this.exitStatus = exitStatus;
        }

        public static JobStatus fromExecution(JobExecution e) {
            return new JobStatus(
                    e.getJobInstance().getId(),
                    e.getJobInstance().getJobName(),
                    e.getStatus(),
                    e.getStartTime(),
                    e.getEndTime(),
                    e.getCreateTime(),
                    e.getLastUpdated(),
                    e.getExitStatus());
        }

        public Long getJobInstanceId() {
            return jobInstanceId;
        }

        public String getJobName() {
            return jobName;
        }

        public BatchStatus getStatus() {
            return status;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public Date getLastUpdated() {
            return lastUpdated;
        }

        public ExitStatus getExitStatus() {
            return exitStatus;
        }
    }
}
