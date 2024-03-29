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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.common.io.Files;
import com.jkoolcloud.tnt4j.config.TrackerConfigStore;
import com.jkoolcloud.tnt4j.streams.configure.StreamsConfigLoader;
import com.jkoolcloud.tnt4j.streams.utils.Utils;
import com.sterlingcommerce.woodstock.event.Event;
import com.sterlingcommerce.woodstock.util.frame.log.Logger;

/**
 * @author akausinis
 * @version 1.0
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("com.sterlingcommerce.woodstock.event.Event")
@PowerMockIgnore({ "javax.net.ssl.*", "javax.security.auth.x500.X500Principal", "javax.management.*" })
public class B2BiSfgEventListenerTest {

	private static final String B2BiDir = "./"; // NON-NLS

	@BeforeClass
	public static void initEnv() {
		System.setProperty("vendorFile", "vendor.properties"); // NON-NLS
	}

	@AfterClass
	public static void cleanupEnv() {
		System.clearProperty("vendorFile"); // NON-NLS
	}

	@Test
	public void testStartStreams() throws Exception {
		System.setProperty(B2BI_TEST_ENV, "true");

		if (StringUtils.isEmpty(System.getProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY))) {
			File streamsConfig = new File(B2BiDir + "../samples/B2Bi/tnt4j-streams-ibm-b2bi.properties");

			System.setProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY, streamsConfig.getAbsolutePath());
		}

		if (StringUtils.isEmpty(System.getProperty("log4j2.configurationFile"))) {
			File log4jConfig = new File(B2BiDir + "/config/log4j2.xml");
			if (!log4jConfig.exists()) {
				throw new RuntimeException();
			}
			System.setProperty("log4j2.configurationFile",
					(SystemUtils.IS_OS_WINDOWS ? "file:///" : "file:/") + log4jConfig.getAbsolutePath());
		}

		if (StringUtils.isEmpty(System.getProperty(TrackerConfigStore.TNT4J_PROPERTIES_KEY))) {
			File tnt4jConfig = new File(B2BiDir + "/config/tnt4j.properties");

			System.setProperty(TrackerConfigStore.TNT4J_PROPERTIES_KEY, tnt4jConfig.getAbsolutePath());
		}

		String exampleFilesPath = System.getProperty("tnt4j.b2biSampleEvents");
		if (exampleFilesPath == null) {
			exampleFilesPath = B2BiDir + "../samples/B2Bi/Events/*.xml"; // NON-NLS
		}

		Path[] exampleFiles = Utils.searchFiles(exampleFilesPath, FileSystems.getDefault()); // NON-NLS

		Logger loggerMock = Mockito.mock(Logger.class, Mockito.RETURNS_MOCKS);
		Whitebox.setInternalState(Event.class, loggerMock);

		B2BiSfgEventListener plugin;
		for (Path filePath : exampleFiles) {
			String fileContent = Files.toString(filePath.toFile(), StandardCharsets.UTF_8);
			Event event = Event.createEvent(fileContent);

			plugin = new B2BiSfgEventListener();
			assertTrue(plugin.isHandled(event.getId(), null, null));
			plugin.handleEvent(event);
		}
		TimeUnit.SECONDS.sleep(20);
	}
}
