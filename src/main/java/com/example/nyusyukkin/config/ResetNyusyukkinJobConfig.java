package com.example.nyusyukkin.config;

import com.example.nyusyukkin.ResetNyusyukkinTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
public class ResetNyusyukkinJobConfig {

	@Bean
	public Step resetNyusyukkinStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			ResetNyusyukkinTasklet tasklet) {
		return new StepBuilder("ResetNyusyukkin", jobRepository).tasklet(tasklet, transactionManager).build();
	}

	@Bean
	public Job resetNyusyukkinJob(JobRepository jobRepository, Step resetNyusyukkinStep,
			JobExecutionListener jobExecutionListener) {
		return new JobBuilder("ResetNyusyukkin", jobRepository).incrementer(new RunIdIncrementer())
			.start(resetNyusyukkinStep)
			.listener(jobExecutionListener)
			.build();
	}

}
