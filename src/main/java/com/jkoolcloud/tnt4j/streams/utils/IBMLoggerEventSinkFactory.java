package com.jkoolcloud.tnt4j.streams.utils;

import java.util.Properties;

import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.custom.inputs.B2BiSfgEventsStream;

public class IBMLoggerEventSinkFactory extends AbstractEventSinkFactory {

	@Override
	public EventSink getEventSink(String name) {
		return configureSink(new IBMLoggerEventSink(B2BiSfgEventsStream.VENDOR_NAME, null));
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return configureSink(new IBMLoggerEventSink(B2BiSfgEventsStream.VENDOR_NAME, null));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return configureSink(new IBMLoggerEventSink(B2BiSfgEventsStream.VENDOR_NAME, null));
	}
}
