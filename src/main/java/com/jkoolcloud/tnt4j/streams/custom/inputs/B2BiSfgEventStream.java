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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SystemUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.StreamsAgent;
import com.jkoolcloud.tnt4j.streams.configure.StreamsConfigLoader;
import com.jkoolcloud.tnt4j.streams.inputs.AbstractBufferedStream;
import com.jkoolcloud.tnt4j.streams.parsers.ActivityParser;
import com.jkoolcloud.tnt4j.streams.utils.B2BiConstants;
import com.jkoolcloud.tnt4j.streams.utils.StreamsResources;
import com.sterlingcommerce.woodstock.event.Event;
import com.sterlingcommerce.woodstock.event.EventListener;
import com.sterlingcommerce.woodstock.event.ExceptionLevel;

public class B2BiSfgEventStream extends AbstractBufferedStream<String> implements EventListener {
	private static final EventSink LOGGER = DefaultEventSinkFactory.defaultEventSink(B2BiSfgEventStream.class);

	private static final long DEFAULT_CACHE_MAX_SIZE = 100;
	private static final long DEFAULT_CACHE_EXPIRE_IN_MINUTES = 10;
	private static final String STREAM_NAME = "TNT4J_B2Bi_Stream"; // NON-NLS

	private static Cache<String, Event> streamedEventsCache;

	private boolean ended;

	public B2BiSfgEventStream() {
		if (streamedEventsCache == null) {
			initStream();
		}
	}

	private synchronized void initStream() {
		setName(STREAM_NAME);
		try {
			checkPrecondition();
			StreamsConfigLoader streamsConfig = new StreamsConfigLoader(
					System.getProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY));
			Collection<ActivityParser> parsers = streamsConfig.getParsers();
			addParsers(parsers);
			StreamsAgent.runFromAPI(this);
			streamedEventsCache = buildCache(DEFAULT_CACHE_MAX_SIZE, DEFAULT_CACHE_EXPIRE_IN_MINUTES);
		} catch (Exception e) {
			LOGGER.log(OpLevel.CRITICAL, StreamsResources.getStringFormatted(B2BiConstants.RESOURCE_BUNDLE_NAME,
					"B2BiSfgEventStream.failed", e.getStackTrace()));
		}
	}

	@Override
	protected EventSink logger() {
		return LOGGER;
	}

	@Override
	protected void start() throws Exception {
		super.start();

		logger().log(OpLevel.DEBUG,
				StreamsResources.getString(StreamsResources.RESOURCE_BUNDLE_NAME, "TNTInputStream.stream.start"),
				getClass().getSimpleName(), getName());
	}

	@Override
	protected boolean isInputEnded() {
		return ended;
	}

	@Override
	protected long getActivityItemByteSize(String item) {
		return item == null ? 0 : item.getBytes().length;
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		addInputToBuffer(event.toXMLString());
		String id = event.getId();
		streamedEventsCache.put(id, event);
		LOGGER.log(OpLevel.TRACE, StreamsResources.getStringFormatted(B2BiConstants.RESOURCE_BUNDLE_NAME,
				"B2BiSfgEventStream.eventRegistered", id));

		if (SchemaKey.WORKFLOW_WF_EVENT_SERVICE_ENDED.key().equals(event.getSchemaKey())) {
			ended = true;
		}
	}

	@Override
	public boolean isHandled(String eventId, String schemaKey, ExceptionLevel exceptionLevel) {
		// TODO:filtering by eventId and schemaKey
		return true;
	}

	private static Cache<String, Event> buildCache(long cSize, long duration) {
		return CacheBuilder.newBuilder().maximumSize(cSize).expireAfterAccess(duration, TimeUnit.MINUTES).build();
	}

	private static void checkPrecondition() throws Exception {
		checkFileFromProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY, ""); // NON-NLS
		checkFileFromProperty("log4j.configuration", SystemUtils.IS_OS_LINUX ? "file:/" : "file:///"); // NON-NLS
		checkFileFromProperty("tnt4j.config", ""); // NON-NLS
	}

	private static void checkFileFromProperty(String propertyKey, String prefix) throws Exception {
		String propertyValue = System.getProperty(propertyKey);
		if (propertyValue == null) {
			throw new IllegalStateException(StreamsResources.getStringFormatted(StreamsResources.RESOURCE_BUNDLE_NAME,
					"TNTInputStream.property.undefined", propertyKey));
		}
		if (!Files.exists(Paths.get(
				prefix == null ? propertyValue : propertyValue.substring(prefix.length(), propertyValue.length())))) {
			throw new IllegalStateException(StreamsResources.getStringFormatted(StreamsResources.RESOURCE_BUNDLE_NAME,
					"StreamsConfig.file.not.found", propertyValue));
		}
	}

}