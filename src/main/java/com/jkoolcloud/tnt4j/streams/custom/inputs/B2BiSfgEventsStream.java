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

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.SystemUtils;

import com.jkoolcloud.tnt4j.config.TrackerConfigStore;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.StreamsAgent;
import com.jkoolcloud.tnt4j.streams.configure.StreamProperties;
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
 * {@link B2BiSfgEventListener#handleEvent(com.sterlingcommerce.woodstock.event.Event)}, so stream configuration does
 * not require stream definition - only parsers configuration is required to map Sterling event data to TNT4J entities
 * fields.
 * <p>
 * This activity stream requires parsers that can support XML data.
 * <p>
 * This activity stream supports properties from {@link AbstractBufferedStream} (and higher hierarchy streams).
 *
 * @version $Revision: 1 $
 *
 * @see com.jkoolcloud.tnt4j.streams.custom.inputs.B2BiSfgEventListener
 */
public class B2BiSfgEventsStream extends AbstractBufferedStream<String> {
	private static final EventSink LOGGER = DefaultEventSinkFactory.defaultEventSink(B2BiSfgEventsStream.class);

	private static final String STREAM_NAME = "TNT4J_B2Bi_Stream"; // NON-NLS
	private static final String PROPS_ROOT_DIR_NAME = "properties"; // NON-NLS
	private static final String PROPS_EXT = ".properties"; // NON-NLS
	private static final String VENDOR_NAME = "jkool"; // NON-NLS
	private static final String APP_PATH = VENDOR_NAME + "/" + version(); // NON-NLS

	private static String ENV_PROPS_DIR_PATH;
	private static String STREAM_PROPERTIES_PATH; // NON-NLS

	private InputStreamListener streamListener = new B2BiStreamListener();

	private boolean ended;
	private static final ReentrantLock lockObject = new ReentrantLock();
	private static final Condition started = lockObject.newCondition();

	/**
	 * Constructs a new B2BiSfgEventsStream.
	 */
	public B2BiSfgEventsStream() {
		ENV_PROPS_DIR_PATH = envPropDirPath();
		STREAM_PROPERTIES_PATH = ENV_PROPS_DIR_PATH + "/" + APP_PATH; // NON-NLS
	}

	/**
	 * Initiates stream.
	 * 
	 * @throws RuntimeException
	 *             if stream initialization fails
	 */
	protected void initStream() throws RuntimeException {
		setName(STREAM_NAME);
		try {
			checkPrecondition();
			StreamsConfigLoader streamsConfig = new StreamsConfigLoader(
					System.getProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY));
			Collection<ActivityParser> parsers = streamsConfig.getParsers();
			addParsers(parsers);

			Map<String, String> props = new HashMap<>(1);
			props.put(StreamProperties.PROP_BUFFER_DROP_WHEN_FULL, "false"); // NON-NLS
			setProperties(props.entrySet());

			StreamsAgent.runFromAPI(streamListener, null, this);
			waitForStreams(TimeUnit.SECONDS.toMillis(30));
		} catch (Exception e) {
			LOGGER.log(OpLevel.CRITICAL,
					StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventsStream.failed"),
					e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private void waitForStreams(long timeOut) throws InterruptedException {
		lockObject.lock();
		try {
			LOGGER.log(OpLevel.DEBUG, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
					"B2BiSfgEventsStream.waiting.for.streams"));
			started.await(timeOut, TimeUnit.MILLISECONDS);
		} finally {
			lockObject.unlock();
		}
	}

	@Override
	protected EventSink logger() {
		return LOGGER;
	}

	@Override
	protected void start() throws Exception {
		super.start();

		LOGGER.log(OpLevel.DEBUG,
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
				"B2BiSfgEventsStream.props.check.checking.for"), propertyKey);
		String propertyValue = System.getProperty(propertyKey);
		if (propertyValue == null) {
			System.setProperty(propertyKey, defaultValue);
			LOGGER.log(OpLevel.TRACE, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
					"B2BiSfgEventsStream.props.check.setting.default"), propertyKey, defaultValue);
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
							"B2BiSfgEventsStream.props.check.file.not.found"),
					propertyValue, Paths.get(".").toAbsolutePath().normalize().toString()); // NON-NLS
		}
	}

	private static void checkPrecondition() throws Exception {
		checkFileFromProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY,
				STREAM_PROPERTIES_PATH + "/tnt4j-streams-ibm-b2bi.properties"); // NON-NLS
		checkFileFromProperty("log4j.configuration", prefixFile(STREAM_PROPERTIES_PATH + "/log4j.properties")); // NON-NLS
		checkFileFromProperty(TrackerConfigStore.TNT4J_PROPERTIES_KEY, STREAM_PROPERTIES_PATH + "/tnt4j.properties"); // NON-NLS
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
	public boolean handleSterlingEvent(Event event) throws Exception {
		try {
			if (SchemaKey.WORKFLOW_WF_EVENT_SERVICE_ENDED.key().equals(event.getSchemaKey())) {
				ended = true;
			}
			return addInputToBuffer(event.toXMLString());
		} catch (Exception exc) {
			LOGGER.log(OpLevel.ERROR, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
					"B2BiSfgEventsStream.buffer.add.failed"), getName(), exc);
			throw exc;
		}
	}

	private static class B2BiStreamListener implements InputStreamListener {

		@Override
		public void onStatusChange(TNTInputStream<?, ?> stream, StreamStatus status) {
			LOGGER.log(OpLevel.DEBUG, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
					"B2BiSfgEventsStream.status.changed"), stream.getName(), status.name());

			if (status.equals(StreamStatus.STARTED)) {
				sendWelcomeMessage(stream);
				started.signal();
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
				ai.setFieldValue(new ActivityField(StreamFieldType.Message.name()), StreamsResources
						.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventsStream.welcome.msg"));
				TNTStreamOutput<ActivityInfo> output = (TNTStreamOutput<ActivityInfo>) stream.getOutput();
				if (output != null) {
					output.logItem(ai);
				} else {
					LOGGER.log(OpLevel.ERROR, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
							"B2BiSfgEventsStream.stream.out.null"));
				}
			} catch (Exception e) {
				LOGGER.log(OpLevel.WARNING, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
						"B2BiSfgEventsStream.welcome.failed"), e);
			}
		}

		@Override
		public void onFailure(TNTInputStream<?, ?> stream, String msg, Throwable exc, String code) {
			LOGGER.log(OpLevel.CRITICAL, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
					"B2BiSfgEventsStream.streams.failed"), stream.getName(), code, msg, exc);
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
	}

	/**
	 * Adds operating system dependent prefix to provided file name. If operating system is Windows prefix is
	 * {@code "file:///"}, {@code "file:/"} - otherwise.
	 *
	 * @param fileName
	 *            file name string
	 * @return complete file name with prefix added
	 */
	public static String prefixFile(String fileName) {
		return (SystemUtils.IS_OS_WINDOWS ? "file:///" : "file:/") + fileName; // NON-NLS
	}

	/**
	 * Builds Sterling environment properties directory path depending on System properties set.
	 *
	 * @return path of Sterling environment properties directory
	 */
	public static String envPropDirPath() {
		LOGGER.log(OpLevel.DEBUG, "--- Running JVM System properties ---"); // NON-NLS
		Properties sProps = System.getProperties();
		for (Map.Entry<?, ?> spe : sProps.entrySet()) {
			LOGGER.log(OpLevel.DEBUG, "{0}", spe); // NON-NLS
		}
		LOGGER.log(OpLevel.DEBUG, "-------------------------------------"); // NON-NLS

		String envPropDirPath = searchForPropsRoot(getSysProperty("PROP_DIR")); // NON-NLS

		if (Utils.isEmpty(envPropDirPath)) {
			envPropDirPath = searchForPropsRoot("."); // NON-NLS
		}
		if (Utils.isEmpty(envPropDirPath)) {
			envPropDirPath = searchForPropsRoot("../.."); // NON-NLS
		}
		if (Utils.isEmpty(envPropDirPath)) {
			envPropDirPath = searchForPropsRoot(getSysProperty("INSTALL_DIR"));// NON-NLS
		}
		if (Utils.isEmpty(envPropDirPath)) {
			envPropDirPath = searchForPropsRoot(getSysProperty("APP_DIR")); // NON-NLS
		}
		if (Utils.isEmpty(envPropDirPath)) {
			envPropDirPath = searchForPropsRoot(getSysProperty("HOME_DIR")); // NON-NLS
		}
		if (Utils.isEmpty(envPropDirPath)) {
			envPropDirPath = searchForPropsRoot(getSysProperty("NOAPP_HOME")); // NON-NLS
		}
		if (Utils.isEmpty(envPropDirPath)) {
			envPropDirPath = searchForPropsRoot(getSysProperty("user.dir")); // NON-NLS
		}

		if (Utils.isEmpty(envPropDirPath)) {
			LOGGER.log(OpLevel.CRITICAL,
					StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventsStream.init.failure"),
					StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
							"B2BiSfgEventsStream.b2bi.props.root.not.found"));
			throw new RuntimeException(StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
					"B2BiSfgEventsStream.b2bi.props.root.not.found"));
		}
		envPropDirPath = envPropDirPath + "/" + PROPS_ROOT_DIR_NAME; // NON-NLS

		LOGGER.log(OpLevel.INFO,
				StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventsStream.b2bi.props.root"),
				envPropDirPath);

		return envPropDirPath;
	}

	private static String getSysProperty(String key) {
		String pValue = System.getProperty(key);
		LOGGER.log(OpLevel.DEBUG, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
				"B2BiSfgEventsStream.checking.sys.property"), key, pValue);

		return pValue;
	}

	private static String searchForPropsRoot(String path) {
		return searchForPropsRoot(path, "/" + PROPS_ROOT_DIR_NAME + "/" + APP_PATH, new ExtensionFilter(PROPS_EXT)); // NON-NLS
	}

	private static String searchForPropsRoot(String path, String pathExt, ExtensionFilter filter) {
		if (Utils.isEmpty(path)) {
			return null;
		}
		File file = new File(path + pathExt);
		boolean exists = file.exists();
		File[] props = exists ? file.listFiles(filter) : null;
		LOGGER.log(OpLevel.DEBUG,
				StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventsStream.props.check.file"),
				file.getAbsolutePath(), exists, (props != null ? props.length : 0));
		if (exists) {
			if (props != null && props.length > 0) {
				return path;
			}
		}
		return searchForPropsRoot(new File(path).getParent(), pathExt, filter);
	}

	protected static String version() {
		Package objPackage = B2BiSfgEventsStream.class.getPackage();
		String version = objPackage.getImplementationVersion();
		version = version.substring(0, version.lastIndexOf('.'));
		LOGGER.log(OpLevel.DEBUG, "--- Resolved {0} package version {1}", VENDOR_NAME, version); // NON-NLS
		return version;
	}
}

class ExtensionFilter implements FilenameFilter {
	String extName;

	ExtensionFilter(String ext) {
		extName = ext;
	}

	@Override
	public boolean accept(File dir, String name) {
		return name.endsWith(extName);
	}
}
