package com.example.batch.file;

import java.util.function.Function;

public final class NoOpConverter implements Function<String, String> {

	@Override
	public String apply(String s) {
		return s;
	}

}
