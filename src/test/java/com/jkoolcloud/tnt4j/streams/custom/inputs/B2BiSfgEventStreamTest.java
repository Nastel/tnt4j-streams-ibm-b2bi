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
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.common.io.Files;
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
@PowerMockIgnore({ "javax.net.ssl.*", "javax.security.auth.x500.X500Principal" })
public class B2BiSfgEventStreamTest {

	private static final String B2BiDir = "./"; // NON-NLS

	@Test
	public void testStartStreams() throws Exception {

		if (Utils.isEmpty(System.getProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY))
				&& Utils.isEmpty(System.getProperty("log4j.configuration"))
				&& Utils.isEmpty(System.getProperty("tnt4j.config"))) {
			final File streamsConfig = new File(B2BiDir + "/samples/B2Bi/tnt4j-streams-ibm-b2bi.properties");
			final File log4jConfig = new File(B2BiDir + "/config/log4j.properties");
			final File tnt4jConfig = new File(B2BiDir + "/config/tnt4j.properties");

			System.setProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY, streamsConfig.getAbsolutePath());
			System.setProperty("log4j.configuration", "file:///" + log4jConfig.getAbsolutePath());
			System.setProperty("tnt4j.config", tnt4jConfig.getAbsolutePath());
		}

		B2BiSfgEventStream plugin = new B2BiSfgEventStream();

		File[] exampleFiles;
		String exampleFilesPath = System.getProperty("tnt4j.b2biSasmpleEvents");
		if (exampleFilesPath == null) {
			exampleFilesPath = B2BiDir + "/samples/B2Bi/Events/*.xml";
		}

		exampleFiles = Utils.searchFiles(exampleFilesPath); // NON-NLS

		Logger loggerMock = Mockito.mock(Logger.class, Mockito.RETURNS_MOCKS);
		Whitebox.setInternalState(Event.class, loggerMock);

		for (File file : exampleFiles) {
			final String fileContent = Files.toString(file, StandardCharsets.UTF_8);
			Event event = Event.createEvent(fileContent);
			Assert.assertTrue(plugin.isHandled(event.getId(), null, null));
			plugin.handleEvent(event);
		}

		Thread.sleep(50000);
	}

}
