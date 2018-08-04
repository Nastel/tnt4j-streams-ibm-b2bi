/*
 * Copyright 2014-2018 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkoolcloud.tnt4j.streams.utils;

/**
 * TNT4J-Streams "B2Bi-plugin" module constants.
 *
 * @version $Revision: 1 $
 */
public final class B2BiConstants {

	/**
	 * Resource bundle name constant for TNT4J-Streams "B2Bi-plugin" module.
	 */
	public static final String RESOURCE_BUNDLE_NAME = "tnt4j-streams-ibm-b2bi-res"; // NON-NLS

	/**
	 * IBM Sterling B2Bi events streaming plugin vendor name.
	 */
	public static final String VENDOR_NAME = "jkool"; // NON-NLS
	/**
	 * Name of system variable witch flags test run, and no native sterling logger should be used
	 */
	public static final String B2BI_TEST_ENV = "b2bi.test.env";

	private B2BiConstants() {
	}
}
