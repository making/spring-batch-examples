/*
 * Copyright (c) 2007 NTT DATA Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.batch.file;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

/**
 * Annotation for input configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface InputFileColumn {

	/**
	 * Column index.<br>
	 * <br>
	 * Indicates the index (order) of the column. <b>Required field</b>.<br>
	 * Column indexes should be implemented to avoid duplication within the same class.
	 */
	int columnIndex();

	/**
	 * Column format.<br>
	 * <br>
	 * Indicates the format of DATE type or BigDecimal type input values.<br>
	 * (Example) "yyyy/MM/dd", "###,###,###"<br>
	 */
	String columnFormat() default "";

	/**
	 * Column byte size.
	 * <p>
	 * Indicates the byte size of each column for fixed-length input/output. Also used
	 * when padding is applied to other file types.
	 * </p>
	 * <b>Required field for fixed-length files.</b><br>
	 * <b>Required field when padding is applied.</b><br>
	 */
	int bytes() default -1;

	/**
	 * Padding type.<br>
	 * <br>
	 * Indicates the padding type (right alignment/left alignment/no padding
	 * [LEFT/RIGHT/NONE]).
	 */
	PaddingType paddingType() default PaddingType.NONE;

	/**
	 * Padding character.<br>
	 * <br>
	 * Indicates the character used for padding. <b>(Only a single half-width character
	 * can be set.)</b><br>
	 * Only the first half-width character of the string set in the Job Bean definition
	 * file is valid as the padding character.<br>
	 * If a full-width character is input, an error occurs and the process terminates.<br>
	 * Even if multiple characters are input, only the first character is used for
	 * processing.<br>
	 * The same character cannot be used for delimiters, enclosing characters, or line
	 * break characters.
	 */
	char paddingChar() default ' ';

	/**
	 * String conversion type.<br>
	 */
	Class<? extends Function<? super String, ? extends String>> stringConverter() default NoOpConverter.class;

	/**
	 * Trim type.<br>
	 * <br>
	 * Indicates the trim type (right trim/left trim/no trim [LEFT/RIGHT/NONE]).
	 */
	TrimType trimType() default TrimType.NONE;

	/**
	 * Trim character.<br>
	 * <br>
	 * Indicates the character to be trimmed. <b>(Only half-width characters can be
	 * set)</b><br>
	 * Only the first half-width character of the string set in the Job Bean definition
	 * file is valid as the trim character.<br>
	 * If a full-width character is input, an error occurs and the process terminates.<br>
	 * Even if multiple characters are input, only the first character is used for
	 * processing.<br>
	 * The same character cannot be used for delimiters, enclosing characters, or line
	 * break characters.
	 */
	char trimChar() default ' ';

	/**
	 * Enclosing character.
	 * <p>
	 * Sets the enclosing character for each column in CSV or variable-length files.
	 * Enclosing characters are limited to half-width characters.<br>
	 * When set to ''\u0000' (minimum value of char type)', the framework determines there
	 * is no enclosing character. The default value is ''\u0000' (minimum value of char
	 * type).
	 * </p>
	 */
	char columnEncloseChar() default Character.MIN_VALUE;

}
