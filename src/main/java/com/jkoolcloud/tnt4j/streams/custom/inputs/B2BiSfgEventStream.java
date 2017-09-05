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
import com.jkoolcloud.tnt4j.format.DefaultFormatter;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.SinkLogEvent;
import com.jkoolcloud.tnt4j.sink.SinkLogEventListener;
import com.jkoolcloud.tnt4j.streams.utils.B2BiConstants;
import com.jkoolcloud.tnt4j.streams.utils.StreamsResources;
import com.sterlingcommerce.woodstock.event.Event;
import com.sterlingcommerce.woodstock.event.EventListener;
import com.sterlingcommerce.woodstock.event.ExceptionLevel;

/**
 * Sterling B2Bi event listener implementation, using {@link B2BiSfqTNTStream} singleton instance to stream
 * {@link #handleEvent(Event)} received {@link Event}'s to JKool Cloud.
 *
 * @version $Revision: 1 $
 */
public class B2BiSfgEventStream implements EventListener {
	private static final EventSink LOGGER = DefaultEventSinkFactory.defaultEventSink(B2BiSfgEventStream.class);

	static {
		SinkLogEventListener logToConsoleEvenSinkListener = new SinkLogEventListener() {
			private final DefaultFormatter formatter = new DefaultFormatter();

			@Override
			public void sinkLogEvent(SinkLogEvent ev) {
				System.out.println(formatter.format(ev.getSinkObject(), ev.getArguments()));
			}
		};

		LOGGER.addSinkLogEventListener(logToConsoleEvenSinkListener);
	}

	private final Object STREAM_INIT_LOCK = new Object();

	private static B2BiSfqTNTStream tntStream;

	/**
	 * Constructs a new B2BiSfgEventStream.
	 */
	public B2BiSfgEventStream() {
		B2BiSfqTNTStream.log(LOGGER, OpLevel.DEBUG,
				StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventStream.create.new"),
				getClass().getName(), hashCode());

		synchronized (STREAM_INIT_LOCK) {
			if (tntStream == null) {
				B2BiSfqTNTStream.log(LOGGER, OpLevel.DEBUG, StreamsResources.getString(
						B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventStream.init.stream.instance.start"));
				tntStream = new B2BiSfqTNTStream();
				tntStream.initStream();
				B2BiSfqTNTStream.log(LOGGER, OpLevel.DEBUG, StreamsResources
						.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventStream.init.stream.instance.end"));
			}
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		synchronized (STREAM_INIT_LOCK) {
			B2BiSfqTNTStream.log(LOGGER, OpLevel.TRACE,
					StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventStream.handle.event"),
					event.toXMLString(), hashCode(), tntStream.hashCode());

			tntStream.handleSterlingEvent(event);
		}
	}

	@Override
	public boolean isHandled(String eventId, String schemaKey, ExceptionLevel exceptionLevel) {
		synchronized (STREAM_INIT_LOCK) {
			B2BiSfqTNTStream.log(LOGGER, OpLevel.TRACE,
					StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventStream.is.handled"),
					eventId, hashCode(), tntStream.hashCode());

			// TODO: filtering by event id and schema key
			return true;
		}
	}

}