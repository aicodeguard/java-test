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
package com.aliyun.api.gateway.demo.service;

/**
 * Jira 配置类
 */
public class JiraConfig {
    /**
     * Jira 服务器地址（例如：https://your-domain.atlassian.net）
     */
    private String jiraBaseUrl;
    
    /**
     * Jira 用户邮箱
     */
    private String username;
    
    /**
     * Jira API Token
     */
    private String apiToken;
    
    /**
     * 请求超时时间（毫秒）
     */
    private int timeout;

    public JiraConfig() {
        this.timeout = 30000; // 默认30秒
    }

    public JiraConfig(String jiraBaseUrl, String username, String apiToken) {
        this.jiraBaseUrl = jiraBaseUrl;
        this.username = username;
        this.apiToken = apiToken;
        this.timeout = 30000;
    }

    public JiraConfig(String jiraBaseUrl, String username, String apiToken, int timeout) {
        this.jiraBaseUrl = jiraBaseUrl;
        this.username = username;
        this.apiToken = apiToken;
        this.timeout = timeout;
    }

    public String getJiraBaseUrl() {
        return jiraBaseUrl;
    }

    public void setJiraBaseUrl(String jiraBaseUrl) {
        this.jiraBaseUrl = jiraBaseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}

