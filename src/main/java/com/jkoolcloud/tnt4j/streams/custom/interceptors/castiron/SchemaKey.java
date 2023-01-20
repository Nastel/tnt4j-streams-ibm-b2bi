/*
 * Copyright 2014-2023 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.streams.custom.interceptors.castiron;

/**
 * List the supported B2Bi schema event keys.
 *
 * @version $Revision: 1 $
 */
public enum SchemaKey {
	/**
	 * Schema key defining {@code "Aft.Visibility.AdminAudit"} event.
	 */
	AFT_VISIBILITY_ADMIN_AUDIT("Aft.Visibility.AdminAudit"), // NON-NLS
	/**
	 * Schema key defining {@code "Aft.Visibility.CommAuthentication"} event.
	 */
	AFT_VISIBILITY_COMM_AUTHENTICATION("Aft.Visibility.CommAuthentication"), // NON-NLS
	/**
	 * Schema key defining {@code "Aft.Visibility.CommAuthorization"} event.
	 */
	AFT_VISIBILITY_COMM_AUTHORIZATION("Aft.Visibility.CommAuthorization"), // NON-NLS
	/**
	 * Schema key defining {@code "Aft.Visibility.CommConnect"} event.
	 */
	AFT_VISIBILITY_COMM_CONNECT("Aft.Visibility.CommConnect"), // NON-NLS
	/**
	 * Schema key defining {@code "Aft.Visibility.CommDisconnect"} event.
	 */
	AFT_VISIBILITY_COMM_DISCONNECT("Aft.Visibility.CommDisconnect"), // NON-NLS
	/**
	 * Schema key defining {@code "Aft.Visibility.CommFileXferBegin"} event.
	 */
	AFT_VISIBILITY_COMM_FILE_XFER_BEGIN("Aft.Visibility.CommFileXferBegin"), // NON-NLS
	/**
	 * Schema key defining {@code "Aft.Visibility.CommFileXferComplete} event.
	 */
	AFT_VISIBILITY_COMM_FILE_XFER_COMPLETE("Aft.Visibility.CommFileXferComplete"), // NON-NLS
	/**
	 * Schema key defining {@code "Aft.Visibility.CommNonTransfer"} event.
	 */
	AFT_VISIBILITY_COMM_NON_TRANSFER("Aft.Visibility.CommNonTransfer"), // NON-NLS
	/**
	 * Schema key defining {@code "Aft.Visibility.CommSessionUpdate"} event.
	 */
	AFT_VISIBILITY_COMM_SESSION_UPDATE("Aft.Visibility.CommSessionUpdate"), // NON-NLS
	/**
	 * Schema key defining {@code "Alert.pingEventListener.pingEventListenerError"} event.
	 */
	ALERT_PING_EVENT_LISTENER_PING_EVENT_LISTENER_ERROR("Alert.pingEventListener.pingEventListenerError"), // NON-NLS
	/**
	 * Schema key defining {@code "ResourceMonitor.ScheduleMonitor.ScheduleDisabled"} event.
	 */
	RESOURCE_MONITOR_SCHEDULE_MONITOR_SCHEDULE_DISABLED("ResourceMonitor.ScheduleMonitor.ScheduleDisabled"), // NON-NLS
	/**
	 * Schema key defining {@code "SystemMonitoring.Security.InternalLogin"} event.
	 */
	SYSTEM_MONITORING_SECURITY_INTERNAL_LOGIN("SystemMonitoring.Security.InternalLogin"), // NON-NLS
	/**
	 * Schema key defining {@code "SystemMonitoring.Security.LoginFailure"} event.
	 */
	SYSTEM_MONITORING_SECURITY_LOGIN_FAILURE("SystemMonitoring.Security.LoginFailure"), // NON-NLS
	/**
	 * Schema key defining {@code "Workflow.WFEvent.BPComplete_MIN"} event.
	 */
	WORKFLOW_WF_EVENT_BP_COMPLETE_MIN("Workflow.WFEvent.BPComplete_MIN"), // NON-NLS
	/**
	 * Schema key defining {@code "Workflow.WFEvent.BPErrorHalt"} event.
	 */
	WORKFLOW_WF_EVENT_BP_ERROR_HALT("Workflow.WFEvent.BPErrorHalt"), // NON-NLS
	/**
	 * Schema key defining {@code "Workflow.WFEvent.BPError"} event.
	 */
	WORKFLOW_WF_EVENT_BP_ERROR("Workflow.WFEvent.BPError"), // NON-NLS
	/**
	 * Schema key defining {@code "Workflow.WFEvent.BPSubStarted_MIN"} event.
	 */
	WORKFLOW_WF_EVENT_BP_SUB_STARTED_MIN("Workflow.WFEvent.BPSubStarted_MIN"), // NON-NLS
	/**
	 * Schema key defining {@code "Workflow.WFEvent.ServiceEnded"} event.
	 */
	WORKFLOW_WF_EVENT_SERVICE_ENDED("Workflow.WFEvent.ServiceEnded"), // NON-NLS
	/**
	 * Schema key defining {@code "Workflow.WFEvent.ServiceStarted"} event.
	 */
	WORKFLOW_WF_EVENT_SERVICE_STARTED("Workflow.WFEvent.ServiceStarted"); // NON-NLS

	private String key;

	SchemaKey(String key) {
		this.key = key;
	}

	/**
	 * Returns event key value (one used in XML).
	 *
	 * @return event key value string
	 */
	public String key() {
		return key;
	}
}
