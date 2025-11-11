/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.aliyun.api.gateway.demo.service.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Jira Issue 更新请求对象
 */
public class JiraIssueUpdateRequest {
    
    private Map<String, Object> fields;

    public JiraIssueUpdateRequest() {
        this.fields = new HashMap<>();
    }

    /**
     * 更新标题
     * @param summary 新标题
     */
    public void setSummary(String summary) {
        fields.put("summary", summary);
    }

    /**
     * 更新描述
     * @param description 新描述
     */
    public void setDescription(String description) {
        // Jira Cloud 使用 Atlassian Document Format
        Map<String, Object> descDoc = new HashMap<>();
        descDoc.put("type", "doc");
        descDoc.put("version", 1);
        
        Map<String, Object> paragraph = new HashMap<>();
        paragraph.put("type", "paragraph");
        
        Map<String, Object> text = new HashMap<>();
        text.put("type", "text");
        text.put("text", description);
        
        paragraph.put("content", new Object[]{text});
        descDoc.put("content", new Object[]{paragraph});
        
        fields.put("description", descDoc);
    }

    /**
     * 更新优先级
     * @param priorityName 优先级名称
     */
    public void setPriority(String priorityName) {
        Map<String, String> priority = new HashMap<>();
        priority.put("name", priorityName);
        fields.put("priority", priority);
    }

    /**
     * 更新经办人
     * @param accountId 用户账号 ID
     */
    public void setAssignee(String accountId) {
        Map<String, String> assignee = new HashMap<>();
        assignee.put("accountId", accountId);
        fields.put("assignee", assignee);
    }

    /**
     * 更新标签
     * @param labels 标签数组
     */
    public void setLabels(String[] labels) {
        fields.put("labels", labels);
    }

    /**
     * 添加自定义字段
     * @param fieldName 字段名称
     * @param value 字段值
     */
    public void addCustomField(String fieldName, Object value) {
        fields.put(fieldName, value);
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }
}

