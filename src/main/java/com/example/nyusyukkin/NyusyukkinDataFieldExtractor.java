package com.example.nyusyukkin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class NyusyukkinDataFieldExtractor implements FieldExtractor<NyusyukkinData> {

	@Override
	public Object[] extract(NyusyukkinData item) {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		return new Object[] { item.getShitenName(), item.getKokyakuId(), item.getNyusyukkinKubun(), item.getKingaku(),
				item.getTorihikibi() != null ? dateFormat.format(item.getTorihikibi()) : "" };
	}

}
