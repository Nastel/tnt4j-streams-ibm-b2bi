package com.jkoolcloud.tnt4j.streams.utils;

import java.util.Properties;

import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;

/**
 * Concrete implementation of {@link com.jkoolcloud.tnt4j.sink.EventSinkFactory} interface to IBM Sterling B2Bi used
 * logging framework, which creates instances of {@link EventSink}. This factory uses {@link B2BiLoggerEventSink} as the
 * underlying logger provider.
 *
 * @see EventSink
 * @see B2BiLoggerEventSink
 *
 * @version $Revision: 1 $
 */
public class B2BiLoggerEventSinkFactory extends AbstractEventSinkFactory {

	@Override
	public EventSink getEventSink(String name) {
		return configureSink(new B2BiLoggerEventSink(B2BiConstants.VENDOR_NAME));
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return configureSink(new B2BiLoggerEventSink(B2BiConstants.VENDOR_NAME));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return configureSink(new B2BiLoggerEventSink(B2BiConstants.VENDOR_NAME));
	}

	/**
	 * Static method to obtain default event sink
	 *
	 * @param name
	 *            name of the application/event sink to get
	 * @return event sink
	 */
	public static EventSink defaultEventSink(String name) {
		return new B2BiLoggerEventSink(name);
	}

	/**
	 * Static method to obtain default event sink
	 *
	 * @param clazz
	 *            class for which to get the event sink
	 * @return event sink
	 */
	public static EventSink defaultEventSink(Class<?> clazz) {
		return defaultEventSink(clazz.getName());
	}
}
