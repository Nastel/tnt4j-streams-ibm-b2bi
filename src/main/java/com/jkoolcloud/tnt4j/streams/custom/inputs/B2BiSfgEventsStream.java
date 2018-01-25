/*
 * Copyright 2014-2018 JKOOL, LLC.
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

import org.apache.commons.lang3.SystemUtils;

import com.jkoolcloud.tnt4j.config.TrackerConfigStore;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.StreamsAgent;
import com.jkoolcloud.tnt4j.streams.configure.StreamProperties;
import com.jkoolcloud.tnt4j.streams.configure.StreamsConfigLoader;
import com.jkoolcloud.tnt4j.streams.inputs.AbstractBufferedStream;
import com.jkoolcloud.tnt4j.streams.parsers.ActivityParser;
import com.jkoolcloud.tnt4j.streams.utils.B2BiConstants;
import com.jkoolcloud.tnt4j.streams.utils.StreamsResources;
import com.jkoolcloud.tnt4j.utils.Utils;
import com.sterlingcommerce.woodstock.event.Event;
import com.sterlingcommerce.woodstock.util.frame.Manager;

/**
 * Implements IBM Sterling B2Bi {@link com.sterlingcommerce.woodstock.event.Event} XML content stream, where each event
 * data is assumed to represent a single activity or event which should be recorded.
 * <p>
 * Incoming IBM Sterling B2Bi events are piped from
 * {@link B2BiSfgEventListener#handleEvent(com.sterlingcommerce.woodstock.event.Event)}, so stream configuration does
 * not require stream definition - only parsers configuration is required to map IBM Sterling B2Bi event data to TNT4J
 * entities fields.
 * <p>
 * This activity stream requires parsers that can support XML data.
 * <p>
 * This activity stream configuration supports properties from {@link AbstractBufferedStream} (and higher hierarchy
 * streams).
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
	private static final String APP_PATH = B2BiConstants.VENDOR_NAME + "/" + version(); // NON-NLS

	private static String ENV_PROPS_DIR_PATH = getPropDirPath();
	private static String STREAM_PROPERTIES_PATH = ENV_PROPS_DIR_PATH + "/" + APP_PATH; // NON-NLS

	private B2BiStreamListener streamListener;
	private boolean ended;

	private static Properties sterlingProperties = null;

	/**
	 * Constructs a new B2BiSfgEventsStream.
	 */
	public B2BiSfgEventsStream() {
		streamListener = new B2BiStreamListener(LOGGER);
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
			if (System.getProperty("test") == null) {
				sterlingProperties = Manager.getProperties(B2BiConstants.VENDOR_NAME);
			}
			checkPrecondition();
			StreamsConfigLoader streamsConfig = new StreamsConfigLoader(
					System.getProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY));
			Collection<ActivityParser> parsers = streamsConfig.getParsers();
			addParsers(parsers);

			Map<String, String> props = new HashMap<>(1);
			props.put(StreamProperties.PROP_BUFFER_DROP_WHEN_FULL, "false"); // NON-NLS
			setProperties(props.entrySet());

			StreamsAgent.runFromAPI(streamListener, null, this);
			streamListener.waitForStart(30, TimeUnit.SECONDS);
		} catch (Exception e) {
			LOGGER.log(OpLevel.CRITICAL, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
					"B2BiSfgEventsStream.failed", e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected EventSink logger() {
		return LOGGER;
	}

	@Override
	protected void start() throws Exception {
		super.start();
		LOGGER.log(OpLevel.INFO, StreamsResources.getBundle(StreamsResources.RESOURCE_BUNDLE_NAME),
				"TNTInputStream.stream.start", getClass().getSimpleName(), getName());
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
		LOGGER.log(OpLevel.TRACE, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
				"B2BiSfgEventsStream.props.check.checking.for", propertyKey);
		String propertyValue = System.getProperty(propertyKey);

		// Check in sterling properties
		if (sterlingProperties == null) {
			LOGGER.log(OpLevel.ERROR, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
					"B2BiSfgEventsStream.props.notFound.sterling", B2BiConstants.VENDOR_NAME);
		} else {
			LOGGER.log(OpLevel.ERROR, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
					"B2BiSfgEventsStream.props.found.sterling.all", B2BiConstants.VENDOR_NAME, sterlingProperties);

			String value = sterlingProperties.getProperty(propertyKey);
			if (value != null) {
				LOGGER.log(OpLevel.INFO, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
						"B2BiSfgEventsStream.props.found.sterling", propertyKey, value);
				System.setProperty(propertyKey, value);
			}
		}

		if (propertyValue == null) {
			System.setProperty(propertyKey, defaultValue);
			LOGGER.log(OpLevel.TRACE, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
					"B2BiSfgEventsStream.props.check.setting.default", propertyKey, defaultValue);
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
			LOGGER.log(OpLevel.TRACE, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
					"B2BiSfgEventsStream.props.check.file.not.found", propertyValue,
					Paths.get(".").toAbsolutePath().normalize().toString()); // NON-NLS
			if (System.getProperty("test") == null) {
				throw new RuntimeException(StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
						"B2BiSfgEventsStream.b2bi.props.root.not.found"));
			}
		}
	}

	private static void checkPrecondition() throws Exception {
		checkFileFromProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY,
				STREAM_PROPERTIES_PATH + "/tnt4j-streams-ibm-b2bi.properties"); // NON-NLS
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
			LOGGER.log(OpLevel.ERROR, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
					"B2BiSfgEventsStream.buffer.add.failed", getName(), exc);
			throw exc;
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
	 * Builds IBM Sterling B2Bi environment properties directory path depending on System properties set.
	 *
	 * @return path of IBM Sterling B2Bi environment properties directory
	 */
	protected static String getPropDirPath() {
		LOGGER.log(OpLevel.INFO, "--- Running JVM System properties ---"); // NON-NLS
		Properties sProps = System.getProperties();
		for (Map.Entry<?, ?> spe : sProps.entrySet()) {
			LOGGER.log(OpLevel.INFO, "{0}", spe); // NON-NLS
		}
		LOGGER.log(OpLevel.INFO, "-------------------------------------"); // NON-NLS

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
		}
		envPropDirPath = envPropDirPath + "/" + PROPS_ROOT_DIR_NAME; // NON-NLS

		LOGGER.log(OpLevel.INFO, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
				"B2BiSfgEventsStream.b2bi.props.root", envPropDirPath);

		return envPropDirPath;
	}

	private static String getSysProperty(String key) {
		String pValue = System.getProperty(key);
		LOGGER.log(OpLevel.DEBUG, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
				"B2BiSfgEventsStream.checking.sys.property", key, pValue);

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
		LOGGER.log(OpLevel.DEBUG, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
				"B2BiSfgEventsStream.props.check.file", file.getAbsolutePath(), exists,
				(props != null ? props.length : 0));
		if (exists) {
			if (props != null && props.length > 0) {
				return path;
			}
		}
		return searchForPropsRoot(new File(path).getParent(), pathExt, filter);
	}

	private static String version() {
		String version = versionFull();
		if (Utils.isEmpty(version)) {
			version = "1.0"; // NON-NLS
			LOGGER.log(OpLevel.DEBUG, "--- Could not resolve package version, defaults to {0}", version); // NON-NLS
		} else {
			version = version.substring(0, version.lastIndexOf('.'));
		}
		LOGGER.log(OpLevel.DEBUG, "--- Resolved {0} package version {1}", B2BiConstants.VENDOR_NAME, version); // NON-NLS
		return version;
	}

	/**
	 * Resolves deployed implementation version of package class belongs to.
	 *
	 * @return package implementation version
	 */
	protected static String versionFull() {
		Package objPackage = B2BiSfgEventsStream.class.getPackage();
		return objPackage.getImplementationVersion();
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
