package com.example.nyusyukkin;

import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

public class NyusyukkinClassifier implements Classifier<NyusyukkinData, ItemWriter<? super NyusyukkinData>> {

	private final ItemWriter<NyusyukkinData> writerNyukin;

	private final ItemWriter<NyusyukkinData> writerSyukkin;

	public NyusyukkinClassifier(ItemWriter<NyusyukkinData> writerNyukin, ItemWriter<NyusyukkinData> writerSyukkin) {
		this.writerNyukin = writerNyukin;
		this.writerSyukkin = writerSyukkin;
	}

	@Override
	public ItemWriter<? super NyusyukkinData> classify(NyusyukkinData item) {
		if (item.getNyusyukkinKubun() == 0) {
			return writerNyukin;
		}
		else if (item.getNyusyukkinKubun() == 1) {
			return writerSyukkin;
		}
		else {
			throw new IllegalArgumentException("Unknown transaction type: " + item.getNyusyukkinKubun());
		}
	}

}