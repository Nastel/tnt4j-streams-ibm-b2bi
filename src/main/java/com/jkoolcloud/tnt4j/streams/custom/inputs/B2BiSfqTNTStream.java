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

import com.jkoolcloud.tnt4j.config.TrackerConfigStore;
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
import com.jkoolcloud.tnt4j.utils.Utils;
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

	private static final int BUFFER_ADD_MAX_RETRY_COUNT = 3;

	private static final String STREAM_NAME = "TNT4J_B2Bi_Stream"; // NON-NLS
	private static final String STREAM_PROPERTIES_PATH = "/jkool/1.0/"; // NON-NLS

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
			e.printStackTrace(System.out);
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

	private static void checkFileFromProperty(String propertyKey, String defaultValue) throws Exception {
		LOGGER.log(OpLevel.TRACE, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
				"B2BiSfqTNTStream.props.check.checking.for"), propertyKey);
		String propertyValue = System.getProperty(propertyKey);
		if (propertyValue == null) {
			System.setProperty(propertyKey, defaultValue);
			LOGGER.log(OpLevel.TRACE, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
					"B2BiSfqTNTStream.props.check.setting.default"), propertyKey, defaultValue);
			propertyValue = defaultValue;
		}

		String sPrefix = prefixFile("");
		String filePath;
		if (propertyValue.startsWith(sPrefix)) {
			filePath = propertyValue.substring(sPrefix.length());
		} else {
			filePath = propertyValue;
		}

		if (!Files.exists(Paths.get(filePath))) {
			LOGGER.log(OpLevel.TRACE,
					StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
							"B2BiSfqTNTStream.props.check.file.not.found"),
					propertyValue, Paths.get(".").toAbsolutePath().normalize().toString()); // NON-NLS
		}
	}

	private static void checkPrecondition() throws Exception {
		String envPropDirPath = System.getProperty("PROP_DIR");
		if (Utils.isEmpty(envPropDirPath)) {
			envPropDirPath = System.getProperty("APP_DIR");
			if (Utils.isEmpty(envPropDirPath)) {
				envPropDirPath = System.getProperty("HOME_DIR");
			}
			if (Utils.isEmpty(envPropDirPath)) {
				envPropDirPath = System.getProperty("INSTALL_DIR");
			}
			if (Utils.isEmpty(envPropDirPath)) {
				envPropDirPath = "."; // NON-NLS
			}

			envPropDirPath += "/properties"; // NON-NLS
		}

		String streamCfgBasePath = envPropDirPath + STREAM_PROPERTIES_PATH;

		checkFileFromProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY,
				streamCfgBasePath + "tnt4j-streams-ibm-b2bi.properties"); // NON-NLS
		checkFileFromProperty("log4j.configuration", prefixFile(streamCfgBasePath + "log4j.properties")); // NON-NLS
		checkFileFromProperty(TrackerConfigStore.TNT4J_PROPERTIES_KEY, streamCfgBasePath + "tnt4j.properties"); // NON-NLS
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
		int rCount = 0;
		while (true) {
			try {
				if (addInputToBuffer(event.toXMLString())) {
					break;
				}
			} catch (Exception exc) {
				LOGGER.log(OpLevel.DEBUG, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
						"B2BiSfqTNTStream.buffer.add.failed"), getName(), started, exc);
				try {
					Thread.sleep(300);
				} catch (InterruptedException ie) {
				}
				if (++rCount == BUFFER_ADD_MAX_RETRY_COUNT) {
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
			LOGGER.log(OpLevel.CRITICAL,
					StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfqTNTStream.streams.failed"),
					stream.getName(), code, msg, exc);
		}
	}

	public static String prefixFile(String fileName) {
		return (SystemUtils.IS_OS_WINDOWS ? "file:///" : "file:/") + fileName;
	}
}
