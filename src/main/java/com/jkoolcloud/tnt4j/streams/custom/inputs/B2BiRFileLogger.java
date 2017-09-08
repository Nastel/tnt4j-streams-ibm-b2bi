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

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.impl.FileEventSinkFactory;


public class B2BiRFileLogger extends FileEventSinkFactory {

	public B2BiRFileLogger(String name) {
		this(name, OpLevel.INFO);
	}

	public B2BiRFileLogger(String name, OpLevel level) {
		super(name);
		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put("Filter", "com.jkoolcloud.tnt4j.filters.EventLevelTimeFilter");
		props.put("Filter.Level", level.toString());
		try {
	        this.setConfiguration(props);
        } catch (ConfigException e) {
	        throw new RuntimeException(e);
        }
	}
}
