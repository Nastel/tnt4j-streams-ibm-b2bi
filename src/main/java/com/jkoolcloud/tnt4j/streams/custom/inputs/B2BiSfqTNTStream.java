package com.jkoolcloud.tnt4j.streams.custom.inputs;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.StreamsAgent;
import com.jkoolcloud.tnt4j.streams.configure.StreamsConfigLoader;
import com.jkoolcloud.tnt4j.streams.fields.ActivityField;
import com.jkoolcloud.tnt4j.streams.fields.ActivityInfo;
import com.jkoolcloud.tnt4j.streams.inputs.AbstractBufferedStream;
import com.jkoolcloud.tnt4j.streams.inputs.InputStreamListener;
import com.jkoolcloud.tnt4j.streams.inputs.StreamStatus;
import com.jkoolcloud.tnt4j.streams.inputs.StreamTasksListener;
import com.jkoolcloud.tnt4j.streams.inputs.TNTInputStream;
import com.jkoolcloud.tnt4j.streams.outputs.TNTStreamOutput;
import com.jkoolcloud.tnt4j.streams.parsers.ActivityParser;
import com.jkoolcloud.tnt4j.streams.utils.B2BiConstants;
import com.jkoolcloud.tnt4j.streams.utils.StreamsResources;

public class B2BiSfqTNTStream extends AbstractBufferedStream<String>{
	private static final EventSink LOGGER = DefaultEventSinkFactory.defaultEventSink(B2BiSfqTNTStream.class);
	
	private boolean ended;
	private static final String STREAM_NAME = "TNT4J_B2Bi_Stream"; // NON-NLS
	private static final String BASE_PROPERTIES_PATH = "./properties/";
	
private StreamTasksListener streamTasksListener = new StreamTasksListener() {
		
		@Override
		public void onReject(TNTInputStream<?, ?> stream, Runnable task) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onDropOff(TNTInputStream<?, ?> stream, List<Runnable> tasks) {
			// TODO Auto-generated method stub
			
		}
	};

	private InputStreamListener streamListener = new InputStreamListener() {
		
		@Override
		public void onSuccess(TNTInputStream<?, ?> stream) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onStreamEvent(TNTInputStream<?, ?> stream, OpLevel level, String message, Object source) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onStatusChange(TNTInputStream<?, ?> stream, StreamStatus status) {
			System.out.println("New TNT4J stream status: " +  status.name());
			if (status.equals(StreamStatus.STARTED)) {
				sendWelcomeMessage(stream);
			}
			
		}
		
		@Override
		public void onProgressUpdate(TNTInputStream<?, ?> stream, int current, int total) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onFinish(TNTInputStream<?, ?> stream, StreamStats stats) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onFailure(TNTInputStream<?, ?> stream, String msg, Throwable exc, String code) {
			// TODO Auto-generated method stub
			
		}
	};
		
		
		protected synchronized void initStream() {
			setName(STREAM_NAME);
			try {
				checkPrecondition();
				StreamsConfigLoader streamsConfig = new StreamsConfigLoader(
						System.getProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY));
				Collection<ActivityParser> parsers = streamsConfig.getParsers();
				addParsers(parsers);
				StreamsAgent.runFromAPI(streamListener, streamTasksListener, this);
			} catch (Exception e) {
				LOGGER.log(OpLevel.CRITICAL, StreamsResources.getStringFormatted(B2BiConstants.RESOURCE_BUNDLE_NAME,
						"B2BiSfgEventStream.failed", e.getStackTrace()));
				e.printStackTrace();
			}
		}
		
		@SuppressWarnings("rawtypes")
		protected static void sendWelcomeMessage(TNTInputStream stream){
			try {
				ActivityInfo ai = new ActivityInfo();
				ai.setFieldValue(new ActivityField(com.jkoolcloud.tnt4j.streams.fields.StreamFieldType.EventType.name()), "EVENT");
				ai.setFieldValue(new ActivityField(com.jkoolcloud.tnt4j.streams.fields.StreamFieldType.Message.name()), "Sterling B2B TNT4J Streams listener sucessfully started");
				final TNTStreamOutput<ActivityInfo> output = stream.getOutput();
				if (output != null) { 
					output.logItem(ai);
				} else {
					System.out.println("Streams not started. No output.");
				}
				
			} catch (Exception e) {
				System.err.println("Failed to welcome. Check your settings!!!!!!!!!!!!");
				e.printStackTrace();
			}
		}
		
		@Override
		protected EventSink logger() {
			return LOGGER;
		}

		@Override
		protected void start() throws Exception {
			super.start();

			logger().log(OpLevel.DEBUG,
					StreamsResources.getString(StreamsResources.RESOURCE_BUNDLE_NAME, "TNTInputStream.stream.start"),
					getClass().getSimpleName(), getName());
		}

		@Override
		protected boolean isInputEnded() {
			return ended;
		}

		@Override
		protected long getActivityItemByteSize(String item) {
			return item == null ? 0 : item.getBytes().length;
		}
		
		private static void checkFileFromProperty(String propertyKey, String prefix, String defaultValue) throws Exception {
			System.out.println("Checking for " + propertyKey);
			String propertyValue = System.getProperty(propertyKey);
			if (propertyValue == null) {
				System.setProperty(propertyKey, defaultValue);
				System.out.println(" >>" + propertyKey + " not found, defaulting: " + defaultValue);
				propertyValue = defaultValue;
			}
			if (!Files.exists(Paths.get(
				prefix == null ? propertyValue : propertyValue.substring(prefix.length(), propertyValue.length())))) {
				System.out.println("File " + propertyValue + "not found. Working path: " + Paths.get(".").toAbsolutePath().normalize().toString() );
			}
		}
		
		private static void checkPrecondition() throws Exception {
			checkFileFromProperty(StreamsConfigLoader.STREAMS_CONFIG_KEY, "", BASE_PROPERTIES_PATH + "tnt-data-source.xml"); // NON-NLS
			checkFileFromProperty("log4j.configuration", SystemUtils.IS_OS_LINUX ? "file:/" : "file:///", SystemUtils.IS_OS_LINUX ? "file:/"+BASE_PROPERTIES_PATH+"/log4j.properties" : "file:///" + BASE_PROPERTIES_PATH + "log4j.properties" ); // NON-NLS
			checkFileFromProperty("tnt4j.config", "", BASE_PROPERTIES_PATH + "tnt4j.properties"); // NON-NLS
		}
		
		public boolean addInputToBuffer(String inputData) {
			System.out.println("Adding event to streams " + hashCode());
			return super.addInputToBuffer(inputData);
		}

		public void informInputEnded(boolean ended) {
			this.ended = ended;
		}
}
