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
 * Jira Issue 创建请求对象
 */
public class JiraIssueCreateRequest {
    
    private Map<String, Object> fields;

    public JiraIssueCreateRequest() {
        this.fields = new HashMap<>();
    }

    /**
     * 设置项目
     * @param projectKey 项目 Key
     */
    public void setProject(String projectKey) {
        Map<String, String> project = new HashMap<>();
        project.put("key", projectKey);
        fields.put("project", project);
    }

    /**
     * 设置标题
     * @param summary 标题
     */
    public void setSummary(String summary) {
        fields.put("summary", summary);
    }

    /**
     * 设置描述
     * @param description 描述内容
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
     * 设置 Issue 类型
     * @param issueTypeName Issue 类型名称（例如：Task, Bug, Story）
     */
    public void setIssueType(String issueTypeName) {
        Map<String, String> issueType = new HashMap<>();
        issueType.put("name", issueTypeName);
        fields.put("issuetype", issueType);
    }

    /**
     * 设置优先级
     * @param priorityName 优先级名称（例如：High, Medium, Low）
     */
    public void setPriority(String priorityName) {
        Map<String, String> priority = new HashMap<>();
        priority.put("name", priorityName);
        fields.put("priority", priority);
    }

    /**
     * 设置经办人
     * @param accountId 用户账号 ID
     */
    public void setAssignee(String accountId) {
        Map<String, String> assignee = new HashMap<>();
        assignee.put("accountId", accountId);
        fields.put("assignee", assignee);
    }

    /**
     * 设置标签
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

