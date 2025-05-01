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

/**
 * Trim type.<br>
 * <br>
 * Indicates the trim type (left trim/right trim/no trim/both sides
 * [LEFT/RIGHT/NONE/BOTH]).
 */
public enum TrimType {

	/**
	 * Left trim
	 */
	LEFT,

	/**
	 * Right trim
	 */
	RIGHT,

	/**
	 * No trim
	 */
	NONE,

	/**
	 * Both sides
	 */
	BOTH

}
