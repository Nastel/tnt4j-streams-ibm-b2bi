/*
 * Copyright 2014-2023 JKOOL, LLC.
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

import java.util.Properties;

import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;

/**
 * Concrete implementation of {@link com.jkoolcloud.tnt4j.sink.EventSinkFactory} interface to IBM Sterling B2Bi used
 * logging framework, which creates instances of {@link EventSink}. This factory uses {@link B2BiLoggerEventSink} as the
 * underlying logger provider.
 *
 * @see EventSink
 * @see B2BiLoggerEventSink
 *
 * @version $Revision: 1 $
 */
public class B2BiLoggerEventSinkFactory extends AbstractEventSinkFactory {

	@Override
	public EventSink getEventSink(String name) {
		return configureSink(new B2BiLoggerEventSink(B2BiConstants.VENDOR_NAME));
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return configureSink(new B2BiLoggerEventSink(B2BiConstants.VENDOR_NAME));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return configureSink(new B2BiLoggerEventSink(B2BiConstants.VENDOR_NAME));
	}

	/**
	 * Static method to obtain default event sink
	 *
	 * @param name
	 *            name of the application/event sink to get
	 * @return event sink
	 */
	public static EventSink defaultEventSink(String name) {
		return new B2BiLoggerEventSink(name);
	}

	/**
	 * Static method to obtain default event sink
	 *
	 * @param clazz
	 *            class for which to get the event sink
	 * @return event sink
	 */
	public static EventSink defaultEventSink(Class<?> clazz) {
		return defaultEventSink(clazz.getName());
	}
}
