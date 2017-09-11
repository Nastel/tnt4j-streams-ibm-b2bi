/*
 * Copyright 2014-2017 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.streams.custom.inputs;

import java.util.HashMap;
import java.util.Map;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.impl.FileEventSinkFactory;

/**
 * TNT4J-Streams IBM Sterling B2Bi logger using file as log entries storage.
 *
 * @version $Revision: 1 $
 */
public class B2BiRFileLogger extends FileEventSinkFactory {

	/**
	 * Constructs a new B2BiRFileLogger.
	 * 
	 * @param name
	 *            file name to write log entries to
	 */
	public B2BiRFileLogger(String name) {
		this(name, OpLevel.INFO);
	}

	/**
	 * Constructs a new B2BiRFileLogger.
	 * 
	 * @param name
	 *            file name to write log entries to
	 * @param level
	 *            minimum logging level
	 */
	public B2BiRFileLogger(String name, OpLevel level) {
		super(name);

		Map<String, Object> props = new HashMap<>(2);
		props.put("Filter", "com.jkoolcloud.tnt4j.filters.EventLevelTimeFilter"); // NON-NLS
		props.put("Filter.Level", level.toString()); // NON-NLS
		try {
			this.setConfiguration(props);
		} catch (ConfigException e) {
			throw new RuntimeException(e);
		}
	}
}
