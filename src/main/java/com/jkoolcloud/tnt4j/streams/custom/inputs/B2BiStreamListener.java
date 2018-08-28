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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.inputs.InputStreamListener;
import com.jkoolcloud.tnt4j.streams.inputs.StreamStatus;
import com.jkoolcloud.tnt4j.streams.inputs.TNTInputStream;
import com.jkoolcloud.tnt4j.streams.utils.B2BiConstants;
import com.jkoolcloud.tnt4j.streams.utils.StreamsResources;

/**
 * {@link B2BiSfgEventsStream} listener capable to lock running thread until stream gets started (changes state to
 * {@link StreamStatus#STARTED}.
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
	 * Locks current {@link Thread} until stream state changes to {@link StreamStatus#STARTED} or
	 * {@link StreamStatus#FAILURE}.
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
			LOGGER.log(OpLevel.INFO, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
					"B2BiSfgEventsStream.waiting.for.streams", timeOut, unit);
			started.await(timeOut, unit);
		} finally {
			lockObject.unlock();
		}
	}

	@Override
	public void onStatusChange(TNTInputStream<?, ?> stream, StreamStatus status) {
		LOGGER.log(OpLevel.INFO, StreamsResources.getBundle(B2BiConstants.RESOURCE_BUNDLE_NAME),
				"B2BiSfgEventsStream.status.changed", stream.getName(), status.name());

		if (status == StreamStatus.STARTED || status == StreamStatus.FAILURE) {
			lockObject.lock();
			try {
				started.signalAll();
			} finally {
				lockObject.unlock();
			}
		}
	}

	@Override
	public void onFinish(TNTInputStream<?, ?> stream) {

	}

	@Override
	public void onFailure(TNTInputStream<?, ?> stream, String msg, Throwable exc, String code) {
		LOGGER.log(OpLevel.CRITICAL,
				StreamsResources.getString(B2BiConstants.RESOURCE_BUNDLE_NAME, "B2BiSfgEventsStream.streams.failed"),
				stream.getName(), StringUtils.defaultIfEmpty(code, ""), StringUtils.defaultIfEmpty(msg, ""),
				exc == null ? "" : exc);
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

}
