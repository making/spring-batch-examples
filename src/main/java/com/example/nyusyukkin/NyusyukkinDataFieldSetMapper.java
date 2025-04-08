package com.example.nyusyukkin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

@Component
public class NyusyukkinDataFieldSetMapper implements FieldSetMapper<NyusyukkinData> {

	@Override
	public NyusyukkinData mapFieldSet(FieldSet fieldSet) throws BindException {
		NyusyukkinData data = new NyusyukkinData();
		data.setShitenName(fieldSet.readString("shitenName"));
		data.setKokyakuId(fieldSet.readString("kokyakuId"));
		data.setNyusyukkinKubun(fieldSet.readInt("nyusyukkinKubun"));
		data.setKingaku(fieldSet.readInt("kingaku"));
		String torihikibi = fieldSet.readString("torihikibi");
		if (torihikibi != null) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				data.setTorihikibi(dateFormat.parse(torihikibi));
			}
			catch (ParseException e) {
				throw new IllegalStateException(e);
			}
		}
		return data;
	}

}
