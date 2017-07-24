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

import org.apache.commons.lang3.SystemUtils;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.StreamsAgent;
import com.jkoolcloud.tnt4j.streams.configure.StreamsConfigLoader;
import com.jkoolcloud.tnt4j.streams.fields.ActivityField;
import com.jkoolcloud.tnt4j.streams.fields.ActivityInfo;
import com.jkoolcloud.tnt4j.streams.fields.StreamFieldType;
import com.jkoolcloud.tnt4j.streams.inputs.AbstractBufferedStream;
import com.jkoolcloud.tnt4j.streams.inputs.InputStreamListener;
import com.jkoolcloud.tnt4j.streams.inputs.StreamStatus;
import com.jkoolcloud.tnt4j.streams.inputs.TNTInputStream;
import com.jkoolcloud.tnt4j.streams.outputs.TNTStreamOutput;
import com.jkoolcloud.tnt4j.streams.parsers.ActivityParser;
import com.jkoolcloud.tnt4j.streams.utils.B2BiConstants;
import com.jkoolcloud.tnt4j.streams.utils.StreamsResources;

public class B2BiSfqTNTStream extends AbstractBufferedStream<String> {
	private static final EventSink LOGGER = DefaultEventSinkFactory.defaultEventSink(B2BiSfqTNTStream.class);

	private static final String STREAM_NAME = "TNT4J_B2Bi_Stream"; // NON-NLS
	private static final String BASE_PROPERTIES_PATH = "./properties/";

	private boolean ended;

	private InputStreamListener streamListener = new InputStreamListener() {
		@Override
		public void onSuccess(TNTInputStream<?, ?> stream) {
		}

		@Override
		public void onStreamEvent(TNTInputStream<?, ?> stream, OpLevel level, String message, Object source) {
		}

		@Override
		public void onStatusChange(TNTInputStream<?, ?> stream, StreamStatus status) {
			LOGGER.log(OpLevel.DEBUG, "New {} stream status: {}", stream.getName(), status.name());

			if (status.equals(StreamStatus.STARTED)) {
				sendWelcomeMessage(stream);
			}
		}

		@Override
		public void onProgressUpdate(TNTInputStream<?, ?> stream, int current, int total) {
		}

		@Override
		public void onFinish(TNTInputStream<?, ?> stream, TNTInputStream.StreamStats stats) {
		}

		@Override
		public void onFailure(TNTInputStream<?, ?> stream, String msg, Throwable exc, String code) {
		}
	};

	protected void initStream() {
		setName(STREAM_NAME);
		try {
			checkPrecondition();
			StreamsConfigLoader streamsConfig = new StreamsConfigLoader(
					System.getProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY));
			Collection<ActivityParser> parsers = streamsConfig.getParsers();
			addParsers(parsers);
			StreamsAgent.runFromAPI(streamListener, null, this);
		} catch (Exception e) {
			LOGGER.log(OpLevel.CRITICAL, StreamsResources.getStringFormatted(B2BiConstants.RESOURCE_BUNDLE_NAME,
					"B2BiSfgEventStream.failed", e.getStackTrace()));
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	protected static void sendWelcomeMessage(TNTInputStream<?, ?> stream) {
		try {
			ActivityInfo ai = new ActivityInfo();
			ai.setFieldValue(new ActivityField(StreamFieldType.EventType.name()), "EVENT");
			ai.setFieldValue(new ActivityField(StreamFieldType.Message.name()),
					"Sterling B2B TNT4J Streams listener sucessfully started");
			TNTStreamOutput<ActivityInfo> output = (TNTStreamOutput<ActivityInfo>) stream.getOutput();
			if (output != null) {
				output.logItem(ai);
			} else {
				LOGGER.log(OpLevel.ERROR, "Streams not started. No output.");
			}

		} catch (Exception e) {
			System.err.println("Failed to welcome. Check your settings!!!!!!!!!!!!");
			e.printStackTrace();
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

	private static void checkFileFromProperty(String propertyKey, String prefix, String defaultValue) throws Exception {
		LOGGER.log(OpLevel.TRACE, "Checking for {}", propertyKey);
		String propertyValue = System.getProperty(propertyKey);
		if (propertyValue == null) {
			System.setProperty(propertyKey, defaultValue);
			LOGGER.log(OpLevel.TRACE, ">> {} not found, defaulting: {}", propertyKey, defaultValue);
			propertyValue = defaultValue;
		}
		if (!Files.exists(Paths.get(
				prefix == null ? propertyValue : propertyValue.substring(prefix.length(), propertyValue.length())))) {
			LOGGER.log(OpLevel.TRACE, "File {} not found. Working path: {}", propertyValue,
					Paths.get(".").toAbsolutePath().normalize().toString());
		}
	}

	private static void checkPrecondition() throws Exception {
		checkFileFromProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY, "", BASE_PROPERTIES_PATH + "tnt-data-source.xml"); // NON-NLS
		checkFileFromProperty("log4j.configuration", SystemUtils.IS_OS_LINUX ? "file:/" : "file:///",
				SystemUtils.IS_OS_LINUX ? "file:/" + BASE_PROPERTIES_PATH + "/log4j.properties"
						: "file:///" + BASE_PROPERTIES_PATH + "log4j.properties"); // NON-NLS
		checkFileFromProperty("tnt4j.config", "", BASE_PROPERTIES_PATH + "tnt4j.properties"); // NON-NLS
	}

	@Override
	public boolean addInputToBuffer(String inputData) {
		return super.addInputToBuffer(inputData);
	}

	public void informInputEnded(boolean ended) {
		this.ended = ended;
	}
}
