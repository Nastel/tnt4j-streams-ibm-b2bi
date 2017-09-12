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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.fields.ActivityField;
import com.jkoolcloud.tnt4j.streams.fields.ActivityInfo;
import com.jkoolcloud.tnt4j.streams.fields.StreamFieldType;
import com.jkoolcloud.tnt4j.streams.inputs.InputStreamListener;
import com.jkoolcloud.tnt4j.streams.inputs.StreamStatus;
import com.jkoolcloud.tnt4j.streams.inputs.TNTInputStream;
import com.jkoolcloud.tnt4j.streams.inputs.TNTInputStream.StreamStats;
import com.jkoolcloud.tnt4j.streams.outputs.TNTStreamOutput;
import com.jkoolcloud.tnt4j.streams.utils.B2BiConstants;
import com.jkoolcloud.tnt4j.streams.utils.StreamsResources;

/**
 * {@link B2BiSfgEventsStream} listener capable to lock running thread until stream gets started (changes state to
 * {@link StreamStatus#STARTED} and sends "welcome" event to jKool for a user to know that IBM B2Bi Sterling events
 * streaming has started.
 *
 * @version $Revision: 1 $
 */
public class B2BiStreamListener implements InputStreamListener {

	private EventSink LOGGER;
	private ReentrantLock lockObject = new ReentrantLock();
	private Condition started = lockObject.newCondition();

	public B2BiStreamListener(EventSink logger) {
		LOGGER = logger;
	}

	/**
	 * Locks current {@link Thread} until stream state changes to {@link StreamStatus#STARTED}.
	 *
	 * @param timeOut
	 *            the maximum time to wait
	 * @param unit
	 *            the time unit of the {@code timeOut} argument
	 * @throws InterruptedException
	 *             if the current thread is interrupted
	 *
	 * @see #onStatusChange(TNTInputStream, StreamStatus)
	 */
	public void waitForStart(long timeOut, TimeUnit unit) throws InterruptedException {
		lockObject.lock();
		try {
			LOGGER.log(OpLevel.INFO, StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME,
					"B2BiSfgEventsStream.waiting.for.streams"), timeOut, unit);
			started.await(timeOut, unit);
		} finally {
			lockObject.unlock();
		}
	}

	@Override
	public void onStatusChange(TNTInputStream<?, ?> stream, StreamStatus status) {
		LOGGER.log(OpLevel.INFO,
				StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventsStream.status.changed"),
				stream.getName(), status.name());

		if (status.equals(StreamStatus.STARTED)) {
			sendWelcomeMessage(stream);
			lockObject.lock();
			try {
				started.signalAll();
			} finally {
				lockObject.unlock();
			}
		}
	}

	/**
	 * Sends "welcome" message to jKool. It is simple event to inform user, that stream has been initialized on Sterling
	 * and is ready to process events.
	 *
	 * @param stream
	 *            the stream instance to send event
	 */
	protected void sendWelcomeMessage(TNTInputStream<?, ?> stream) {
		try {
			ActivityInfo ai = new ActivityInfo();
			ai.setFieldValue(new ActivityField(StreamFieldType.EventType.name()), "EVENT"); // NON-NLS
			ai.setFieldValue(new ActivityField(StreamFieldType.Message.name()),
					StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventsStream.welcome.msg"));
			@SuppressWarnings("unchecked")
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
		LOGGER.log(OpLevel.CRITICAL,
				StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventsStream.streams.failed"),
				stream.getName(), code, msg, exc);
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
