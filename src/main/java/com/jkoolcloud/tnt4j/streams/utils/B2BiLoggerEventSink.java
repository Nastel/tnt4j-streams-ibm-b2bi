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

package com.jkoolcloud.tnt4j.streams.utils;

import java.io.IOException;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.sink.AbstractEventSink;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.sterlingcommerce.woodstock.util.frame.log.LogLevel;
import com.sterlingcommerce.woodstock.util.frame.log.LogService;
import com.sterlingcommerce.woodstock.util.frame.log.Logger;

/**
 * {@link com.jkoolcloud.tnt4j.sink.EventSink} implementation that routes B2Bi stream log messages to IBM Sterling B2Bi
 * used logging framework.
 *
 * @see TrackingEvent
 * @see AbstractEventSink
 * @see OpLevel
 *
 * @version $Revision: 1 $
 */
public class B2BiLoggerEventSink extends AbstractEventSink {

	private Logger logger;

	/**
	 * Creates a new B2Bi logger sink.
	 *
	 * @param name
	 *            logger sink name
	 */
	public B2BiLoggerEventSink(String name) {
		super(name);
		_open();
	}

	@Override
	protected void _log(TrackingEvent event) throws IOException {
		switch (event.getSeverity()) {
		case HALT:
		case FATAL:
		case CRITICAL:
			logger.logCritical(String.valueOf(event));
			break;
		case FAILURE:
		case ERROR:
			logger.logError(String.valueOf(event));
			break;
		case DEBUG:
			logger.logDebug(String.valueOf(event));
			break;
		case TRACE:
			logger.logCommTrace(String.valueOf(event));
			break;
		case NOTICE:
		case WARNING:
			logger.logWarn(String.valueOf(event));
			break;
		case NONE:
			break;
		case INFO:
		default:
			logger.logInfo(String.valueOf(event));
			break;
		}
	}

	@Override
	protected void _log(TrackingActivity activity) throws IOException {
		switch (activity.getSeverity()) {
		case HALT:
		case FATAL:
		case CRITICAL:
			logger.logCritical(String.valueOf(activity));
			break;
		case FAILURE:
		case ERROR:
			logger.logError(String.valueOf(activity));
			break;
		case DEBUG:
			logger.logDebug(String.valueOf(activity));
			break;
		case TRACE:
			logger.logCommTrace(String.valueOf(activity));
			break;
		case NOTICE:
		case WARNING:
			logger.logWarn(String.valueOf(activity));
			break;
		case NONE:
			break;
		case INFO:
		default:
			logger.logInfo(String.valueOf(activity));
			break;
		}
	}

	@Override
	protected void _log(Snapshot snapshot) throws IOException {
		switch (snapshot.getSeverity()) {
		case HALT:
		case FATAL:
		case CRITICAL:
			logger.logCritical(String.valueOf(snapshot));
			break;
		case FAILURE:
		case ERROR:
			logger.logError(String.valueOf(snapshot));
			break;
		case DEBUG:
			logger.logDebug(String.valueOf(snapshot));
			break;
		case TRACE:
			logger.logCommTrace(String.valueOf(snapshot));
			break;
		case NOTICE:
		case WARNING:
			logger.logWarn(String.valueOf(snapshot));
			break;
		case NONE:
			break;
		case INFO:
		default:
			logger.logInfo(String.valueOf(snapshot));
			break;
		}
	}

	@Override
	protected void _log(long ttl, Source src, OpLevel sev, String msg, Object... args) throws IOException {
		switch (sev) {
		case HALT:
		case FATAL:
		case CRITICAL:
			logger.logCritical(Utils.format(String.valueOf(msg), args));
			break;
		case FAILURE:
		case ERROR:
			logger.logError(Utils.format(String.valueOf(msg), args));
			break;
		case DEBUG:
			logger.logDebug(Utils.format(String.valueOf(msg), args));
			break;
		case TRACE:
			logger.logCommTrace(Utils.format(String.valueOf(msg), args));
			break;
		case NOTICE:
		case WARNING:
			logger.logWarn(Utils.format(String.valueOf(msg), args));
			break;
		case NONE:
			break;
		case INFO:
		default:
			logger.logInfo(Utils.format(String.valueOf(msg), args));
			break;
		}
	}

	@Override
	protected void _write(Object msg, Object... args) throws IOException, InterruptedException {
		logger.log(Utils.format(String.valueOf(msg), args));
	}

	@Override
	public boolean isSet(OpLevel sev) {
		if (logger.getLogLevel().equals(LogLevel.ALL.toString())) {
			return true;
		}
		switch (sev) {
		case HALT:
		case FATAL:
		case CRITICAL:
			return logger.getLogLevel().equals(LogLevel.CRITICAL.toString());
		case FAILURE:
		case ERROR:
			return logger.getLogLevel().equals(LogLevel.ERROR.toString());
		case DEBUG:
			return logger.getLogLevel().equals(LogLevel.DEBUG.toString());
		case TRACE:
			return logger.getLogLevel().equals(LogLevel.COMMTRACE.toString());
		case NOTICE:
		case WARNING:
			return logger.getLogLevel().equals(LogLevel.WARN.toString());
		case NONE:
			return logger.getLogLevel().equals(LogLevel.NONE.toString());
		case INFO:
		default:
			return logger.getLogLevel().equals(LogLevel.INFO.toString());
		}
	}

	@Override
	public Object getSinkHandle() {
		return logger;
	}

	@Override
	public boolean isOpen() {
		return logger != null;
	}

	@Override
	protected synchronized void _open() {
		logger = LogService.getLogger(B2BiConstants.VENDOR_NAME);
		logger.log("B2Bi Logger EventSink open: vendor=" + B2BiConstants.VENDOR_NAME + ", name=" + getName());
	}

	@Override
	protected void _close() throws IOException {
		logger = null;
	}
}
