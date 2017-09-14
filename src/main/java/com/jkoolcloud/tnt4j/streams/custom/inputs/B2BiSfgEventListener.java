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
 * {@link #handleEvent(Event)} received {@link Event}'s to JKool Cloud.
 *
 * @version $Revision: 1 $
 */
public class B2BiSfgEventListener implements EventListener {
	private static final EventSink LOGGER;
	private static final B2BiSfgEventsStream tntStream;

	static {
		// initialize logging
		if (System.getProperty("test") == null) {
			EventSinkFactory loggerFactory = new B2BiLoggerEventSinkFactory();
			DefaultEventSinkFactory.setDefaultEventSinkFactory(loggerFactory);
		}
		LOGGER = DefaultEventSinkFactory.defaultEventSink(B2BiSfgEventListener.class);

		// initialize stream
		LOGGER.log(OpLevel.INFO, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
				"B2BiSfgEventListener.init.stream.instance.start"), B2BiSfgEventsStream.versionFull());
		tntStream = new B2BiSfgEventsStream();
		tntStream.initStream();
		LOGGER.log(OpLevel.INFO, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
				"B2BiSfgEventListener.init.stream.instance.end"));
	}

	/**
	 * Constructs a new B2BiSfgEventListener.
	 */
	public B2BiSfgEventListener() {
		LOGGER.log(OpLevel.INFO,
				StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventListener.create.new"),
				getClass().getName(), hashCode());
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		LOGGER.log(OpLevel.TRACE,
				StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventListener.handle.event"),
				event.toXMLString(), hashCode(), tntStream.hashCode());

		tntStream.handleSterlingEvent(event);
	}

	@Override
	public boolean isHandled(String eventId, String schemaKey, ExceptionLevel exceptionLevel) {
		LOGGER.log(OpLevel.TRACE,
				StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventListener.is.handled"),
				eventId, hashCode(), tntStream.hashCode());

		// TODO: filtering by event id and schema key
		return true;
	}
}