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
	private final Object STREAM_INIT_LOCK = new Object();

	private static B2BiSfqTNTStream tntStream;

	/**
	 * Constructs a new B2BiSfgEventStream.
	 */
	public B2BiSfgEventStream() {
		LOGGER.log(OpLevel.DEBUG, "Creating new {} instance {}", getClass().getName(), hashCode());

		synchronized (STREAM_INIT_LOCK) {
			if (tntStream == null) {
				LOGGER.log(OpLevel.DEBUG, "Initializing B2BiSfqTNTStream instance to be used...");
				tntStream = new B2BiSfqTNTStream();
				tntStream.initStream();
				LOGGER.log(OpLevel.DEBUG, "B2BiSfqTNTStream initialized!");
			}
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		LOGGER.log(OpLevel.TRACE, "B2BiSfgEventStream.handleEvent: event={}, listener={}, stream={}", event, hashCode(),
				tntStream.hashCode());
		System.out.println("B2BiSfgEventStream.handleEvent: " + event.getId() + " stream: " + hashCode());
		System.out.println(event.toXMLString());

		tntStream.handleSterlingEvent(event);
	}

	@Override
	public boolean isHandled(String eventId, String schemaKey, ExceptionLevel exceptionLevel) {
		LOGGER.log(OpLevel.TRACE, "B2BiSfgEventStream.isHandled: event={}, listener={}, stream={}", eventId, hashCode(),
				tntStream.hashCode());

		// TODO: filtering by event id and schema key
		return true;
	}

}