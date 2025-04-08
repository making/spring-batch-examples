package com.example.batch;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

@Component
public class BatchExitHandler implements JobExecutionListener, ExitCodeGenerator {

	private int exitCode = 0;

	@Override
	public void beforeJob(JobExecution jobExecution) {
		// No action needed before the job starts
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		// Set exit code to 255 if the job failed
		if (jobExecution.getStatus() == BatchStatus.FAILED) {
			exitCode = 255;
		}
	}

	@Override
	public int getExitCode() {
		// Provide the exit code to SpringApplication.exit()
		return exitCode;
	}

}
