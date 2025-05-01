package com.example.nyusyukkin.config;

import com.example.batch.file.InputFileColumnLineMapper;
import com.example.nyusyukkin.NyusyukkinData;
import com.example.nyusyukkin.NyusyukkinMapper;
import java.util.List;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration(proxyBeanMethods = false)
public class ImportNyusyukkinDataJobConfig {

	// ItemReader definition (read from CSV file)
	@Bean
	@StepScope
	public FlatFileItemReader<NyusyukkinData> nyusyukkinDataFileItemReader(
			@Value("#{jobParameters['inputFile'] ?: 'https://github.com/terasoluna-batch/terasoluna-sample/raw/refs/heads/master/terasoluna-batch-tutorial/inputFile/SMP002_input.csv'}") Resource resource) {
		return new FlatFileItemReaderBuilder<NyusyukkinData>().name("nyusyukkinItemReader")
			.resource(resource)
			.linesToSkip(0)
			.lineMapper(new InputFileColumnLineMapper<>(NyusyukkinData.class, ","))
			.encoding("Windows-31J")
			.build();
	}

	@Bean
	public ItemWriter<NyusyukkinData> nyusyukkinDataItemWriter(NyusyukkinMapper nyusyukkinMapper) {
		return chunk -> nyusyukkinMapper.insertNyusyukkinDataBatch((List<NyusyukkinData>) chunk.getItems());
	}

	@Bean
	@StepScope
	public BeanValidatingItemProcessor<NyusyukkinData> nyusyukkinDataItemProcessor(
			LocalValidatorFactoryBean localValidatorFactoryBean) {
		BeanValidatingItemProcessor<NyusyukkinData> validatingItemProcessor = new BeanValidatingItemProcessor<>(
				localValidatorFactoryBean);
		validatingItemProcessor.setFilter(true);
		return validatingItemProcessor;
	}

	@Bean
	public Step importNyusyukkinDataStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			FlatFileItemReader<NyusyukkinData> nyusyukkinDataFileItemReader,
			BeanValidatingItemProcessor<NyusyukkinData> nyusyukkinDataItemProcessor,
			ItemWriter<NyusyukkinData> nyusyukkinDataItemWriter) {
		return new StepBuilder("ImportNyusyukkinData", jobRepository)
			.<NyusyukkinData, NyusyukkinData>chunk(1000, transactionManager)
			.reader(nyusyukkinDataFileItemReader)
			.processor(nyusyukkinDataItemProcessor)
			.writer(nyusyukkinDataItemWriter)
			.build();

	}

	@Bean
	public Job importNyusyukkinDataJob(JobRepository jobRepository, Step importNyusyukkinDataStep) {
		return new JobBuilder("ImportNyusyukkinData", jobRepository).start(importNyusyukkinDataStep).build();
	}

}
