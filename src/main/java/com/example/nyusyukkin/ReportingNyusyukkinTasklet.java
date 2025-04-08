package com.example.nyusyukkin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.support.SingleItemPeekableItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class ReportingNyusyukkinTasklet implements Tasklet {

	private final SingleItemPeekableItemReader<NyusyukkinData> itemReader;

	private final FlatFileItemWriter<NyusyukkinFileOutput> itemWriter;

	public ReportingNyusyukkinTasklet(SingleItemPeekableItemReader<NyusyukkinData> itemReader,
			FlatFileItemWriter<NyusyukkinFileOutput> itemWriter) {
		this.itemReader = itemReader;
		this.itemWriter = itemWriter;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		// Open
		ExecutionContext executionContext = contribution.getStepExecution().getExecutionContext();
		try {
			this.itemReader.open(executionContext);
			this.itemWriter.open(executionContext);

			// Counter for deposits
			int nyukinNum = 0;
			// Counter for withdrawals
			int syukkinNum = 0;
			// Total amount of deposits
			int nyukinSum = 0;
			// Total amount of withdrawals
			int syukkinSum = 0;
			List<NyusyukkinFileOutput> items = new ArrayList<>();
			while (this.itemReader.peek() != null) {
				NyusyukkinData data = this.itemReader.read();
				if (data != null && data.getNyusyukkinKubun() == 0) {
					syukkinNum++;
					syukkinSum += data.getKingaku();
				}
				else if (data != null && data.getNyusyukkinKubun() == 1) {
					nyukinNum++;
					nyukinSum += data.getKingaku();
				}

				NyusyukkinData nextData = this.itemReader.peek();
				if (data != null && (isTorihikibiDifferent(data, nextData) || isShitenNameDifferent(data, nextData))) {
					NyusyukkinFileOutput outputData = new NyusyukkinFileOutput();
					outputData.setTorihikibi(data.getTorihikibi());
					outputData.setShitenName(data.getShitenName());
					outputData.setNyukinNum(nyukinNum);
					outputData.setNyukinSum(nyukinSum);
					outputData.setSyukkinNum(syukkinNum);
					outputData.setSyukkinSum(syukkinSum);
					items.add(outputData);
					this.itemWriter.write(new Chunk<>(outputData));
					nyukinNum = 0;
					syukkinNum = 0;
					nyukinSum = 0;
					syukkinSum = 0;
				}
			}
		}
		finally {
			try {
				this.itemReader.close();
			}
			catch (ItemStreamException ignored) {
			}
			try {
				this.itemWriter.close();
			}
			catch (ItemStreamException ignored) {

			}
		}
		return RepeatStatus.FINISHED;
	}

	boolean isTorihikibiDifferent(NyusyukkinData data1, NyusyukkinData data2) {
		Date date1 = data1 != null ? data1.getTorihikibi() : null;
		Date date2 = data2 != null ? data2.getTorihikibi() : null;
		return !Objects.equals(date1, date2);
	}

	boolean isShitenNameDifferent(NyusyukkinData data1, NyusyukkinData data2) {
		String s1 = data1 != null ? data1.getShitenName() : null;
		String s2 = data2 != null ? data2.getShitenName() : null;
		return !Objects.equals(s1, s2);
	}

}
