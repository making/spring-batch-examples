package com.example.nyusyukkin.config;

import com.example.batch.file.OutputFileColumnFieldExtractor;
import com.example.nyusyukkin.NyusyukkinClassifier;
import com.example.nyusyukkin.NyusyukkinData;
import com.example.nyusyukkin.NyusyukkinMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.builder.ClassifierCompositeItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
public class ExportNyusyukkinDataJobConfig {

	// Reader using MyBatisCursorItemReader with the builder pattern
	@Bean
	@StepScope
	public MyBatisCursorItemReader<NyusyukkinData> nyusyukkinDataItemReader(SqlSessionFactory sqlSessionFactory) {
		MyBatisCursorItemReader<NyusyukkinData> itemReader = new MyBatisCursorItemReaderBuilder<NyusyukkinData>()
			.sqlSessionFactory(sqlSessionFactory)
			.queryId(NyusyukkinMapper.class.getName() + ".selectNyusyukkinData")
			.build();
		itemReader.setName("nyusyukkinDataReader");
		return itemReader;
	}

	// Writer for deposit transactions (nyusyukkinKubun == 0)
	@Bean
	@StepScope
	public FlatFileItemWriter<NyusyukkinData> writerNyukin() {
		return new FlatFileItemWriterBuilder<NyusyukkinData>().name("writerNyukin")
			.resource(new FileSystemResource("outputFile/SMP001_output_nyukin.csv"))
			.encoding("Windows-31J")
			.append(false)
			.delimited()
			.delimiter(",")
			.fieldExtractor(new OutputFileColumnFieldExtractor<>(NyusyukkinData.class))
			.build();
	}

	// Writer for withdrawal transactions (nyusyukkinKubun == 1)
	@Bean
	@StepScope
	public FlatFileItemWriter<NyusyukkinData> writerSyukkin() {
		return new FlatFileItemWriterBuilder<NyusyukkinData>().name("writerSyukkin")
			.resource(new FileSystemResource("outputFile/SMP001_output_syukkin.csv"))
			.encoding("Windows-31J")
			.append(false)
			.delimited()
			.delimiter(",")
			.fieldExtractor(new OutputFileColumnFieldExtractor<>(NyusyukkinData.class))
			.build();
	}

	// Composite writer that delegates to the appropriate writer based on nyusyukkinKubun
	// using builder pattern
	@Bean
	@StepScope
	public ClassifierCompositeItemWriter<NyusyukkinData> nyusyukkinFileItemWriter(
			FlatFileItemWriter<NyusyukkinData> writerNyukin, FlatFileItemWriter<NyusyukkinData> writerSyukkin) {
		return new ClassifierCompositeItemWriterBuilder<NyusyukkinData>()
			.classifier(new NyusyukkinClassifier(writerNyukin, writerSyukkin))
			.build();
	}

	@Bean
	public Step exportNyusyukkinDataStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			MyBatisCursorItemReader<NyusyukkinData> nyusyukkinDataItemReader,
			ClassifierCompositeItemWriter<NyusyukkinData> nyusyukkinFileItemWriter,
			FlatFileItemWriter<NyusyukkinData> writerNyukin, FlatFileItemWriter<NyusyukkinData> writerSyukkin) {
		return new StepBuilder("ExportNyusyukkinData", jobRepository)
			.<NyusyukkinData, NyusyukkinData>chunk(1000, transactionManager)
			.reader(nyusyukkinDataItemReader)
			.writer(nyusyukkinFileItemWriter)
			.stream(writerNyukin)
			.stream(writerSyukkin)
			.build();

	}

	@Bean
	public Job exportNyusyukkinDataJob(JobRepository jobRepository, Step exportNyusyukkinDataStep) {
		return new JobBuilder("ExportNyusyukkinData", jobRepository).start(exportNyusyukkinDataStep).build();
	}

}
