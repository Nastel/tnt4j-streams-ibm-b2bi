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

import static org.junit.Assert.fail;

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
import com.jkoolcloud.tnt4j.streams.utils.Utils;
import com.sterlingcommerce.woodstock.event.Event;
import com.sterlingcommerce.woodstock.util.frame.log.Logger;

/**
 * @author akausinis
 * @version 1.0
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("com.sterlingcommerce.woodstock.event.Event")
@PowerMockIgnore("javax.net.ssl.*")
public class B2BiSfgEventStreamTest {

	private static final String baseDir = "../"; // NON-NLS
	private static final String B2BiDir = "./"; // NON-NLS

	@Test
	public void testHandleEvent() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsHandled() {
		fail("Not yet implemented");
	}

	@Test
	public void testStartStreams() throws Exception {

		final File streamsConfig = new File(B2BiDir + "/samples/B2Bi/tnt-data-source.xml");
		final File log4jConfig = new File(baseDir + "/config/log4j.properties");
		final File tnt4jConfig = new File(baseDir + "/config/tnt4j.properties");

		System.setProperty("streams.config", streamsConfig.getAbsolutePath());
		System.setProperty("log4j.configuration", "file:///" + log4jConfig.getAbsolutePath());
		System.setProperty("tnt4j.config", tnt4jConfig.getAbsolutePath());

		B2BiSfgEventStream plugin = new B2BiSfgEventStream();

		final File[] exampleFiles = Utils.searchFiles(B2BiDir + "/samples/B2Bi/Events/*.xml"); // NON-NLS

		Logger loggerMock = Mockito.mock(Logger.class, Mockito.RETURNS_MOCKS);
		Whitebox.setInternalState(Event.class, loggerMock);

		String lastEvent = null;
		for (File file : exampleFiles)
		// File file = exampleFiles[0];
		{
			final String fileContent = Files.toString(file, StandardCharsets.UTF_8);
			Event event = Event.createEvent(fileContent);
			plugin.handleEvent(event);
			if (lastEvent != null) {
				Assert.assertTrue(plugin.isHandled(lastEvent, null, null));
			}
			lastEvent = event.getId();
		}

		Thread.sleep(50000);
	}

}
