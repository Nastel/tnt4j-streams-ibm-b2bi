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
import com.jkoolcloud.tnt4j.format.DefaultFormatter;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.SinkLogEvent;
import com.jkoolcloud.tnt4j.sink.SinkLogEventListener;
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
import com.sterlingcommerce.woodstock.event.Event;

/**
 * Implements IBM Sterling B2Bi {@link com.sterlingcommerce.woodstock.event.Event} XML content stream, where each event
 * data is assumed to represent a single activity or event which should be recorded.
 * <p>
 * Incoming Sterling events are piped from
 * {@link B2BiSfgEventStream#handleEvent(com.sterlingcommerce.woodstock.event.Event)}, so stream configuration does not
 * require stream definition - only parsers configuration is required to map Sterling event data to TNT4J entities
 * fields.
 * <p>
 * This activity stream requires parsers that can support XML data.
 * <p>
 * This activity stream supports properties from {@link AbstractBufferedStream} (and higher hierarchy streams).
 *
 * @version $Revision: 1 $
 *
 * @see com.jkoolcloud.tnt4j.streams.custom.inputs.B2BiSfgEventStream
 */
public class B2BiSfqTNTStream extends AbstractBufferedStream<String> {
	private static final EventSink LOGGER = DefaultEventSinkFactory.defaultEventSink(B2BiSfqTNTStream.class);

	private static final DefaultFormatter formatter = new DefaultFormatter();
	static SinkLogEventListener logToConsoleEvenSinkListener = new SinkLogEventListener() {
		@Override
		public void sinkLogEvent(SinkLogEvent ev) {
			System.out.println(formatter.format(ev.getSinkObject(), ev.getArguments()));
		}
	};
	static {
		LOGGER.addSinkLogEventListener(logToConsoleEvenSinkListener);
	}

	private static final String STREAM_NAME = "TNT4J_B2Bi_Stream"; // NON-NLS
	private static final String BASE_PROPERTIES_PATH = "./properties/"; // NON-NLS

	private InputStreamListener streamListener = new B2BiTNTStreamListener();

	private boolean ended;
	private static boolean started;

	/**
	 * Initiates stream.
	 */
	protected void initStream() {
		setName(STREAM_NAME);
		try {
			checkPrecondition();
			StreamsConfigLoader streamsConfig = new StreamsConfigLoader(
					System.getProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY));
			Collection<ActivityParser> parsers = streamsConfig.getParsers();
			addParsers(parsers);
			StreamsAgent.runFromAPI(streamListener, null, this);
			while (true) {
				Thread.sleep(500);
				LOGGER.log(OpLevel.DEBUG, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
						"B2BiSfqTNTStream.waiting.for.streams"));
				if (started) {
					break;
				}
			}
		} catch (Exception e) {
			LOGGER.log(OpLevel.CRITICAL,
					StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfqTNTStream.failed"),
					e.getLocalizedMessage(), e);
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
		LOGGER.log(OpLevel.TRACE, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
				"B2BiSfqTNTStream.props.check.checking.for"), propertyKey);
		String propertyValue = System.getProperty(propertyKey);
		if (propertyValue == null) {
			System.setProperty(propertyKey, defaultValue);
			LOGGER.log(OpLevel.TRACE, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
					"B2BiSfqTNTStream.props.check.setting.default"), propertyKey, defaultValue);
			propertyValue = defaultValue;
		}
		if (!Files.exists(Paths.get(
				prefix == null ? propertyValue : propertyValue.substring(prefix.length(), propertyValue.length())))) {
			LOGGER.log(OpLevel.TRACE,
					StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
							"B2BiSfqTNTStream.props.check.file.not.found"),
					propertyValue, Paths.get(".").toAbsolutePath().normalize().toString()); // NON-NLS
		}
	}

	private static void checkPrecondition() throws Exception {
		checkFileFromProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY, "", BASE_PROPERTIES_PATH + "tnt-data-source.xml"); // NON-NLS
		checkFileFromProperty("log4j.configuration", SystemUtils.IS_OS_LINUX ? "file:/" : "file:///", // NON-NLS
				SystemUtils.IS_OS_LINUX ? "file:/" + BASE_PROPERTIES_PATH + "/log4j.properties" // NON-NLS
						: "file:///" + BASE_PROPERTIES_PATH + "log4j.properties"); // NON-NLS
		checkFileFromProperty("tnt4j.config", "", BASE_PROPERTIES_PATH + "tnt4j.properties"); // NON-NLS
	}

	/**
	 * Handles IBM Sterling B2Bi event. Adds event XML to input buffer and marks stream "logical" data input end if
	 * event has {@code "Workflow.WFEvent.ServiceEnded"} schema key.
	 *
	 * @param event
	 *            event instance to handle
	 * @throws Exception
	 *             if adding event XML data to buffer fails
	 */
	public void handleSterlingEvent(Event event) throws Exception {

		// TODO check is really needed.
		int count = 0;
		int maxTries = 3;
		boolean success = false;
		while (true) {
			try {
				success = addInputToBuffer(event.toXMLString());
				if (success) {
					break;
				}
			} catch (Exception exc) {
				LOGGER.log(OpLevel.DEBUG, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
						"B2BiSfqTNTStream.buffer.add.failed"), getName(), started, exc);
				try {
					Thread.sleep(300);
				} catch (InterruptedException ie) {
				}
				if (++count == maxTries) {
					throw exc;
				}
			}
		}

		if (SchemaKey.WORKFLOW_WF_EVENT_SERVICE_ENDED.key().equals(event.getSchemaKey())) {
			ended = true;
		}
	}

	private static class B2BiTNTStreamListener implements InputStreamListener {

		@Override
		public void onStatusChange(TNTInputStream<?, ?> stream, StreamStatus status) {
			LOGGER.log(OpLevel.DEBUG,
					StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfqTNTStream.status.changed"),
					stream.getName(), status.name());

			if (status.equals(StreamStatus.STARTED)) {
				sendWelcomeMessage(stream);
				started = true;
			}
		}

		/**
		 * Sends "welcome" message to jKool. It is simple event to inform user, that stream has been initialized on
		 * Sterling and is ready to process events.
		 *
		 * @param stream
		 *            the stream instance to send event
		 */
		@SuppressWarnings("unchecked")
		protected static void sendWelcomeMessage(TNTInputStream<?, ?> stream) {
			try {
				ActivityInfo ai = new ActivityInfo();
				ai.setFieldValue(new ActivityField(StreamFieldType.EventType.name()), "EVENT"); // NON-NLS
				ai.setFieldValue(new ActivityField(StreamFieldType.Message.name()),
						StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfqTNTStream.welcome.msg"));
				TNTStreamOutput<ActivityInfo> output = (TNTStreamOutput<ActivityInfo>) stream.getOutput();
				if (output != null) {
					output.logItem(ai);
				} else {
					LOGGER.log(OpLevel.ERROR, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
							"B2BiSfqTNTStream.stream.out.null"));
				}
			} catch (Exception e) {
				LOGGER.log(OpLevel.WARNING, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
						"B2BiSfqTNTStream.welcome.failed"), e);
			}
		}

		@Override
		public void onSuccess(TNTInputStream<?, ?> stream) {
		}

		@Override
		public void onStreamEvent(TNTInputStream<?, ?> stream, OpLevel level, String message, Object source) {
		}

		@Override
		public void onProgressUpdate(TNTInputStream<?, ?> stream, int current, int total) {
		}

		@Override
		public void onFinish(TNTInputStream<?, ?> stream, StreamStats stats) {
		}

		@Override
		public void onFailure(TNTInputStream<?, ?> stream, String msg, Throwable exc, String code) {
		}
	}
}
