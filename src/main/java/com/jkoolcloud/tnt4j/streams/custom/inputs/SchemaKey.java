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

/**
 * List the supported B2Bi schema keys.
 *
 * @version $Revision: 1 $
 */
public enum SchemaKey {
	AFT_VISIBILITY_ADMIN_AUDIT("Aft.Visibility.AdminAudit"), // NON-NLS
	AFT_VISIBILITY_COMM_AUTHENTICATION("Aft.Visibility.CommAuthentication"), // NON-NLS
	AFT_VISIBILITY_COMM_AUTHORIZATION("Aft.Visibility.CommAuthorization"), // NON-NLS
	AFT_VISIBILITY_COMM_CONNECT("Aft.Visibility.CommConnect"), // NON-NLS
	AFT_VISIBILITY_COMM_DISCONNECT("Aft.Visibility.CommDisconnect"), // NON-NLS
	AFT_VISIBILITY_COMM_FILE_XFER_BEGIN("Aft.Visibility.CommFileXferBegin"), // NON-NLS
	AFT_VISIBILITY_COMM_FILE_XFER_COMPLETE("Aft.Visibility.CommFileXferComplete"), // NON-NLS
	AFT_VISIBILITY_COMM_NON_TRANSFER("Aft.Visibility.CommNonTransfer"), // NON-NLS
	AFT_VISIBILITY_COMM_SESSION_UPDATE("Aft.Visibility.CommSessionUpdate"), // NON-NLS
	ALERT_PING_EVENT_LISTENER_PING_EVENT_LISTENER_ERROR("Alert.pingEventListener.pingEventListenerError"), // NON-NLS
	RESOURCE_MONITOR_SCHEDULE_MONITOR_SCHEDULE_DISABLED("ResourceMonitor.ScheduleMonitor.ScheduleDisabled"), // NON-NLS
	SYSTEM_MONITORING_SECURITY_INTERNAL_LOGIN("SystemMonitoring.Security.InternalLogin"), // NON-NLS
	SYSTEM_MONITORING_SECURITY_LOGIN_FAILURE("SystemMonitoring.Security.LoginFailure"), // NON-NLS
	WORKFLOW_WF_EVENT_BP_COMPLETE_MIN("Workflow.WFEvent.BPComplete_MIN"), // NON-NLS
	WORKFLOW_WF_EVENT_BP_ERROR_HALT("Workflow.WFEvent.BPErrorHalt"), // NON-NLS
	WORKFLOW_WF_EVENT_BP_ERROR("Workflow.WFEvent.BPError"), // NON-NLS
	WORKFLOW_WF_EVENT_BP_SUB_STARTED_MIN("Workflow.WFEvent.BPSubStarted_MIN"), // NON-NLS
	WORKFLOW_WF_EVENT_SERVICE_ENDED("Workflow.WFEvent.ServiceEnded"), // NON-NLS
	WORKFLOW_WF_EVENT_SERVICE_STARTED("Workflow.WFEvent.ServiceStarted"); // NON-NLS

	private String key;

	SchemaKey(String key) {
		this.key = key;
	}

	public String key() {
		return key;
	}
}
