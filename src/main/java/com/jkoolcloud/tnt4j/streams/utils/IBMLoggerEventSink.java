package com.jkoolcloud.tnt4j.streams.utils;

import static com.jkoolcloud.tnt4j.streams.custom.inputs.B2BiSfgEventsStream.VENDOR_NAME;

import java.io.IOException;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSink;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.sterlingcommerce.woodstock.util.frame.log.LogLevel;
import com.sterlingcommerce.woodstock.util.frame.log.LogService;
import com.sterlingcommerce.woodstock.util.frame.log.Logger;

public class IBMLoggerEventSink extends AbstractEventSink {

	private Logger logger;

	public IBMLoggerEventSink(String nm) {
		super(nm);
	}

	public IBMLoggerEventSink(String nm, EventFormatter frmt) {
		super(nm, frmt);
		open();
	}

	@Override
	protected void _log(TrackingEvent event) throws Exception {
		switch (event.getSeverity()) {
		case HALT:
		case FATAL:
		case CRITICAL:
		case FAILURE:
		case ERROR:
			logger.logError(String.valueOf(event));
			break;
		case DEBUG:
			logger.logDebug(String.valueOf(event));
			break;
		case INFO:
		case NONE:
			logger.logInfo(String.valueOf(event));
			break;
		case TRACE:
			logger.logCommTrace(String.valueOf(event));
			break;
		case NOTICE:
		case WARNING:
			logger.logWarn(String.valueOf(event));
			break;
		default:
			logger.logInfo(String.valueOf(event));
			break;
		}
	}

	@Override
	protected void _log(TrackingActivity activity) throws Exception {
		switch (activity.getSeverity()) {
		case HALT:
		case FATAL:
		case CRITICAL:
		case FAILURE:
		case ERROR:
			logger.logError(String.valueOf(activity));
			break;
		case DEBUG:
			logger.logDebug(String.valueOf(activity));
			break;
		case INFO:
		case NONE:
			logger.logInfo(String.valueOf(activity));
			break;
		case TRACE:
			logger.logCommTrace(String.valueOf(activity));
			break;
		case NOTICE:
		case WARNING:
			logger.logWarn(String.valueOf(activity));
			break;
		default:
			logger.logInfo(String.valueOf(activity));
			break;
		}
	}

	@Override
	protected void _log(Snapshot snapshot) throws Exception {
		switch (snapshot.getSeverity()) {
		case HALT:
		case FATAL:
		case CRITICAL:
		case FAILURE:
		case ERROR:
			logger.logError(String.valueOf(snapshot));
			break;
		case DEBUG:
			logger.logDebug(String.valueOf(snapshot));
			break;
		case INFO:
		case NONE:
			logger.logInfo(String.valueOf(snapshot));
			break;
		case TRACE:
			logger.logCommTrace(String.valueOf(snapshot));
			break;
		case NOTICE:
		case WARNING:
			logger.logWarn(String.valueOf(snapshot));
			break;
		default:
			logger.logInfo(String.valueOf(snapshot));
			break;
		}
	}

	@Override
	protected void _log(long ttl, Source src, OpLevel sev, String msg, Object... args) throws Exception {
		switch (sev) {
		case HALT:
		case FATAL:
		case CRITICAL:
		case FAILURE:
		case ERROR:
			logger.logError(Utils.format(String.valueOf(msg), args));
			break;
		case DEBUG:
			logger.logDebug(Utils.format(String.valueOf(msg), args));
			break;
		case INFO:
		case NONE:
			logger.logInfo(Utils.format(String.valueOf(msg), args));
			break;
		case TRACE:
			logger.logCommTrace(Utils.format(String.valueOf(msg), args));
			break;
		case NOTICE:
		case WARNING:
			logger.logWarn(String.valueOf(Utils.format(String.valueOf(msg), args)));
			break;
		default:
			logger.logInfo(String.valueOf(Utils.format(String.valueOf(msg), args)));
			break;
		}
	}

	@Override
	protected void _write(Object msg, Object... args) throws IOException, InterruptedException {
		logger.log(Utils.format(String.valueOf(msg), args));
	}

	@Override
	public boolean isSet(OpLevel sev) {
		if (logger.getLogLevel().equals(LogLevel.ALL.toString()))
			return true;
		switch (sev) {
		case HALT:
		case FATAL:
		case CRITICAL:
		case FAILURE:
		case ERROR:
			return logger.getLogLevel().equals(LogLevel.ERROR.toString());
		case DEBUG:
			return logger.getLogLevel().equals(LogLevel.DEBUG.toString());
		case INFO:
		case NONE:
			return logger.getLogLevel().equals(LogLevel.NONE.toString());
		case TRACE:
			return logger.getLogLevel().equals(LogLevel.COMMTRACE.toString());
		case NOTICE:
		case WARNING:
			return logger.getLogLevel().equals(LogLevel.WARN.toString());
		default:
			return logger.getLogLevel().equals(LogLevel.ERROR.toString());
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
	public synchronized void open() {
		logger = LogService.getLogger(VENDOR_NAME);
		logger.log("Logger EventSink ready");
	}

	@Override
	public void close() throws IOException {
		logger = null;
	}
}
