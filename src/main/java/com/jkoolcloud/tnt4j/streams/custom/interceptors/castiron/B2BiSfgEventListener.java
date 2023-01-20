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

package com.jkoolcloud.tnt4j.streams.custom.interceptors.castiron;

import static com.jkoolcloud.tnt4j.streams.utils.B2BiConstants.B2BI_TEST_ENV;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.EventSinkFactory;
import com.jkoolcloud.tnt4j.streams.utils.B2BiConstants;
import com.jkoolcloud.tnt4j.streams.utils.B2BiLoggerEventSinkFactory;
import com.jkoolcloud.tnt4j.streams.utils.StreamsResources;
import com.sterlingcommerce.woodstock.event.Event;
import com.sterlingcommerce.woodstock.event.EventListener;
import com.sterlingcommerce.woodstock.event.ExceptionLevel;

/**
 * IBM Sterling B2Bi event listener implementation, using {@link B2BiSfgEventsStream} singleton instance to stream
 * {@link #handleEvent(Event)} received {@link Event}'s to jKoolCloud.
 *
 * @version $Revision: 1 $
 */
public class B2BiSfgEventListener implements EventListener {
	private static EventSink LOGGER;
	private static B2BiSfgEventsStream tntStream;

	static {
		_initStreams();
	}

	private static void _initStreams() {
		try {
			// initialize logging
			if (!Boolean.getBoolean(B2BI_TEST_ENV)) {
				EventSinkFactory loggerFactory = new B2BiLoggerEventSinkFactory();
				DefaultEventSinkFactory.setDefaultEventSinkFactory(loggerFactory);
			}
			LOGGER = DefaultEventSinkFactory.defaultEventSink(B2BiSfgEventListener.class);
			// initialize stream
			LOGGER.log(OpLevel.INFO, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
					"B2BiSfgEventListener.init.stream.instance.start", B2BiSfgEventsStream.versionFull());
			tntStream = new B2BiSfgEventsStream();
			tntStream.initStream();
			LOGGER.log(OpLevel.INFO, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
					"B2BiSfgEventListener.init.stream.instance.end");
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Constructs a new B2BiSfgEventListener.
	 */
	public B2BiSfgEventListener() {
		if (LOGGER != null) {
			LOGGER.log(OpLevel.INFO, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
					"B2BiSfgEventListener.create.new", getClass().getName(), hashCode());
		} else {
			throw new RuntimeException(this.getClass().getName() + " not initialized");
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (tntStream != null) {
			LOGGER.log(OpLevel.TRACE, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
					"B2BiSfgEventListener.handle.event", event.toXMLString(), hashCode(), tntStream.hashCode());
			tntStream.handleSterlingEvent(event);
		} else {
			throw new RuntimeException(this.getClass().getName() + " not initialized");
		}
	}

	@Override
	public boolean isHandled(String eventId, String schemaKey, ExceptionLevel exceptionLevel) {
		if (LOGGER != null) {
			LOGGER.log(OpLevel.TRACE, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
					"B2BiSfgEventListener.is.handled", eventId, hashCode(), tntStream.hashCode());
			return true;
		} else {
			throw new RuntimeException(this.getClass().getName() + " not initialized");
		}
	}
}