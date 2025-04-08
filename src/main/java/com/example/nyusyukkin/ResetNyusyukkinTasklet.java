package com.example.nyusyukkin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class ResetNyusyukkinTasklet implements Tasklet {

	private final Logger logger = LoggerFactory.getLogger(ResetNyusyukkinTasklet.class);

	private final NyusyukkinMapper nyusyukkinMapper;

	public ResetNyusyukkinTasklet(NyusyukkinMapper nyusyukkinMapper) {
		this.nyusyukkinMapper = nyusyukkinMapper;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		logger.info("Nyusyukkin tasklet started");
		// Number of rows to create in the DB. Default is 100.
		int maxNumber = 1000;
		List<NyusyukkinData> dataList = new ArrayList<>(maxNumber);

		// Random generator for data creation
		Random random = new Random();


		// Clear existing records from the deposit/withdrawal table
		this.nyusyukkinMapper.deleteNyusyukkinData();

		for (int count = 1; count <= maxNumber; count++) {
			NyusyukkinData nyusyukkin = new NyusyukkinData();

			// Randomly determine branch name
			String shitenName = "";
			int shitenNum = random.nextInt(3) + 1;
			if (shitenNum == 1) {
				shitenName = "東京";
			}
			else if (shitenNum == 2) {
				shitenName = "埼玉";
			}
			else {
				shitenName = "千葉";
			}
			StringBuilder kokyakuId = new StringBuilder(String.valueOf(random.nextInt(1000) + 1));

			// Padding with leading zeros
			while (kokyakuId.length() < 4) {
				kokyakuId.insert(0, "0");
			}
			int nyusyukkinKubun = random.nextInt(2);
			int kingaku = random.nextInt(1000000) + 1;

			// Generate random date between 2010/01/01 and 2011/12/31
			StringBuilder hiduke = new StringBuilder();
			hiduke.append(2023 + random.nextInt(2)).append("/");
			int month = random.nextInt(12) + 1;
			hiduke.append(month).append("/");
			if (month == 4 || month == 6 || month == 9 || month == 11) {
				hiduke.append(random.nextInt(30) + 1);
			}
			else if (month == 2) {
				hiduke.append(random.nextInt(28) + 1);
			}
			else {
				hiduke.append(random.nextInt(31) + 1);
			}
			Date date = new SimpleDateFormat("yyyy/MM/dd").parse(hiduke.toString());

			// Create one data object
			nyusyukkin.setShitenName(shitenName);
			nyusyukkin.setKokyakuId(kokyakuId.toString());
			nyusyukkin.setNyusyukkinKubun(nyusyukkinKubun);
			nyusyukkin.setKingaku(kingaku);
			nyusyukkin.setTorihikibi(date);

			dataList.add(nyusyukkin);
		}
		this.nyusyukkinMapper.insertNyusyukkinDataBatch(dataList);
		logger.info("Nyusyukkin tasklet completed");
		return RepeatStatus.FINISHED;
	}

}
