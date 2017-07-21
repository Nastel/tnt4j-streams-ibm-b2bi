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

import com.sterlingcommerce.woodstock.event.Event;
import com.sterlingcommerce.woodstock.event.EventListener;
import com.sterlingcommerce.woodstock.event.ExceptionLevel;

public class B2BiSfgEventStream implements EventListener {

	private static B2BiSfqTNTStream tntStream;

	public B2BiSfgEventStream() {
		System.out.println("New " + this.getClass().getName() + " " + hashCode());
		if (tntStream == null) {
			System.out.println("Starting TNT4J event streams");
			tntStream = new B2BiSfqTNTStream();
			tntStream.initStream();
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		System.out.println("tnt4j.handleEvent: " + event.getId() + " stream: " + hashCode());
		tntStream.addInputToBuffer(event.toXMLString());
		if (SchemaKey.WORKFLOW_WF_EVENT_SERVICE_ENDED.key().equals(event.getSchemaKey())) {
			tntStream.informInputEnded(true);
		}
	}

	@Override
	public boolean isHandled(String eventId, String schemaKey, ExceptionLevel exceptionLevel) {
		System.out.print("tnt4j.isHandled: " + eventId + " stream: " + hashCode());
		return true;
	}

}