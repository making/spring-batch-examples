package com.example.nyusyukkin.config;

import com.example.batch.file.InputFileColumnLineMapper;
import com.example.batch.file.OutputFileColumnFieldExtractor;
import com.example.nyusyukkin.NyusyukkinData;
import com.example.nyusyukkin.NyusyukkinFileOutput;
import com.example.nyusyukkin.ReportingNyusyukkinTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.support.SingleItemPeekableItemReader;
import org.springframework.batch.item.support.builder.SingleItemPeekableItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
public class ReportingNyusyukkinJobConfig {

	@Bean
	@StepScope
	public FlatFileItemReader<NyusyukkinData> delegateNyusyukkinDataItemReader(
			@Value("#{jobParameters['inputFile'] ?: 'https://github.com/terasoluna-batch/terasoluna-sample/raw/refs/heads/master/terasoluna-batch-tutorial/inputFile/SMP004_input.csv'}") Resource resource) {
		return new FlatFileItemReaderBuilder<NyusyukkinData>().name("nyusyukkinItemReader")
			.resource(resource)
			.linesToSkip(0)
			.lineMapper(new InputFileColumnLineMapper<>(NyusyukkinData.class, ","))
			.encoding("Windows-31J")
			.build();
	}

	@Bean
	@StepScope
	public SingleItemPeekableItemReader<NyusyukkinData> nyusyukkinDataPeekableItemReader(
			FlatFileItemReader<NyusyukkinData> delegateNyusyukkinDataItemReader) {
		return new SingleItemPeekableItemReaderBuilder<NyusyukkinData>().delegate(delegateNyusyukkinDataItemReader)
			.build();
	}

	@Bean
	@StepScope
	public FlatFileItemWriter<NyusyukkinFileOutput> nyusyukkinFileOutputItemWriter() {
		return new FlatFileItemWriterBuilder<NyusyukkinFileOutput>().name("nyusyukkinFileOutputItemWriter")
			.resource(new FileSystemResource("outputFile/SMP004_output.csv"))
			.encoding("Windows-31J")
			.append(false)
			.delimited()
			.delimiter(",")
			.fieldExtractor(new OutputFileColumnFieldExtractor<>(NyusyukkinFileOutput.class))
			.build();
	}

	@Bean
	public Step reportingNyusyukkinStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			ReportingNyusyukkinTasklet tasklet) {
		return new StepBuilder("ReportingNyusyukkin", jobRepository).tasklet(tasklet, transactionManager).build();
	}

	@Bean
	public Job reportingNyusyukkinJob(JobRepository jobRepository, Step reportingNyusyukkinStep,
			JobExecutionListener jobExecutionListener) {
		return new JobBuilder("ReportingNyusyukkin", jobRepository).incrementer(new RunIdIncrementer())
			.start(reportingNyusyukkinStep)
			.listener(jobExecutionListener)
			.build();
	}

}
