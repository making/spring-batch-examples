package com.example.nyusyukkin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.stereotype.Component;

@Component
public class NyusyukkinFileOutputFieldExtractor implements FieldExtractor<NyusyukkinFileOutput> {

	@Override
	public Object[] extract(NyusyukkinFileOutput item) {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		return new Object[] { item.getTorihikibi() != null ? dateFormat.format(item.getTorihikibi()) : "",
				item.getShitenName(), item.getNyukinNum(), item.getSyukkinNum(), item.getNyukinSum(),
				item.getSyukkinSum() };
	}

}
