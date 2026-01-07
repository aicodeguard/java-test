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

import com.alibaba.fastjson.JSON;
import com.aliyun.api.gateway.demo.service.dto.JiraIssue;
import com.aliyun.api.gateway.demo.service.dto.JiraIssueCreateRequest;
import com.aliyun.api.gateway.demo.service.dto.JiraIssueUpdateRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Jira API 服务类
 * 提供与 Jira REST API 交互的方法
 */
public class JiraService {
    
    private final JiraConfig config;
    private final String authHeader;
    
    private static final String API_VERSION = "/rest/api/3";
    
    /**
     * 构造函数
     * @param config Jira配置对象
     */
    public JiraService(JiraConfig config) {
        this.config = config;
        // 创建 Basic Auth 认证头
        String auth = config.getUsername() + ":" + config.getApiToken();
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        this.authHeader = "Basic " + new String(encodedAuth);
    }
    
    /**
     * 创建 Issue
     * @param request 创建 Issue 的请求对象
     * @return 创建的 Issue 信息
     * @throws Exception
     */
    public JiraIssue createIssue(JiraIssueCreateRequest request) throws Exception {
        String url = config.getJiraBaseUrl() + API_VERSION + "/issue";
        String jsonBody = JSON.toJSONString(request);
        
        String response = executePost(url, jsonBody);
        return JSON.parseObject(response, JiraIssue.class);
    }
    
    /**
     * 获取 Issue 详情
     * @param issueIdOrKey Issue ID 或 Key（例如：PROJ-123）
     * @return Issue 详情
     * @throws Exception
     */
    public JiraIssue getIssue(String issueIdOrKey) throws Exception {
        String url = config.getJiraBaseUrl() + API_VERSION + "/issue/" + issueIdOrKey;
        String response = executeGet(url);
        return JSON.parseObject(response, JiraIssue.class);
    }
    
    /**
     * 更新 Issue
     * @param issueIdOrKey Issue ID 或 Key
     * @param request 更新请求对象
     * @return 是否更新成功
     * @throws Exception
     */
    public boolean updateIssue(String issueIdOrKey, JiraIssueUpdateRequest request) throws Exception {
        String url = config.getJiraBaseUrl() + API_VERSION + "/issue/" + issueIdOrKey;
        String jsonBody = JSON.toJSONString(request);
        
        executePut(url, jsonBody);
        return true;
    }
    
    /**
     * 删除 Issue
     * @param issueIdOrKey Issue ID 或 Key
     * @return 是否删除成功
     * @throws Exception
     */
    public boolean deleteIssue(String issueIdOrKey) throws Exception {
        String url = config.getJiraBaseUrl() + API_VERSION + "/issue/" + issueIdOrKey;
        executeDelete(url);
        return true;
    }
    
    /**
     * 搜索 Issues
     * @param jql JQL 查询语句
     * @param maxResults 最大返回结果数
     * @return 搜索结果的 JSON 字符串
     * @throws Exception
     */
    public String searchIssues(String jql, int maxResults) throws Exception {
        String url = config.getJiraBaseUrl() + API_VERSION + "/search";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("jql", jql);
        requestBody.put("maxResults", maxResults);
        
        String jsonBody = JSON.toJSONString(requestBody);
        return executePost(url, jsonBody);
    }
    
    /**
     * 为 Issue 添加评论
     * @param issueIdOrKey Issue ID 或 Key
     * @param comment 评论内容
     * @return 评论结果的 JSON 字符串
     * @throws Exception
     */
    public String addComment(String issueIdOrKey, String comment) throws Exception {
        String url = config.getJiraBaseUrl() + API_VERSION + "/issue/" + issueIdOrKey + "/comment";
        
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        body.put("type", "doc");
        body.put("version", 1);
        
        Map<String, Object> content = new HashMap<>();
        content.put("type", "paragraph");
        
        Map<String, Object> text = new HashMap<>();
        text.put("type", "text");
        text.put("text", comment);
        
        content.put("content", new Object[]{text});
        body.put("content", new Object[]{content});
        
        requestBody.put("body", body);
        
        String jsonBody = JSON.toJSONString(requestBody);
        return executePost(url, jsonBody);
    }
    
    /**
     * 获取所有项目
     * @return 项目列表的 JSON 字符串
     * @throws Exception
     */
    public String getAllProjects() throws Exception {
        String url = config.getJiraBaseUrl() + API_VERSION + "/project";
        return executeGet(url);
    }
    
    /**
     * 获取项目详情
     * @param projectIdOrKey 项目 ID 或 Key
     * @return 项目详情的 JSON 字符串
     * @throws Exception
     */
    public String getProject(String projectIdOrKey) throws Exception {
        String url = config.getJiraBaseUrl() + API_VERSION + "/project/" + projectIdOrKey;
        return executeGet(url);
    }
    
    /**
     * 转换 Issue 状态
     * @param issueIdOrKey Issue ID 或 Key
     * @param transitionId 转换 ID
     * @return 是否转换成功
     * @throws Exception
     */
    public boolean transitionIssue(String issueIdOrKey, String transitionId) throws Exception {
        String url = config.getJiraBaseUrl() + API_VERSION + "/issue/" + issueIdOrKey + "/transitions";
        
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, String> transition = new HashMap<>();
        transition.put("id", transitionId);
        requestBody.put("transition", transition);
        
        String jsonBody = JSON.toJSONString(requestBody);
        executePost(url, jsonBody);
        return true;
    }
    
    /**
     * 执行 GET 请求
     */
    private String executeGet(String url) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        
        // 设置请求头
        httpGet.setHeader("Authorization", authHeader);
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("Accept", "application/json");
        
        try {
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
            
            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new Exception("Jira API Error: " + statusCode + " - " + responseBody);
            }
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
    
    /**
     * 执行 POST 请求
     */
    private String executePost(String url, String jsonBody) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        
        // 设置请求头
        httpPost.setHeader("Authorization", authHeader);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");
        
        // 设置请求体
        if (jsonBody != null && !jsonBody.isEmpty()) {
            StringEntity entity = new StringEntity(jsonBody, "UTF-8");
            httpPost.setEntity(entity);
        }
        
        try {
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
            
            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new Exception("Jira API Error: " + statusCode + " - " + responseBody);
            }
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
    
    /**
     * 执行 PUT 请求
     */
    private String executePut(String url, String jsonBody) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPut httpPut = new HttpPut(url);
        
        // 设置请求头
        httpPut.setHeader("Authorization", authHeader);
        httpPut.setHeader("Content-Type", "application/json");
        httpPut.setHeader("Accept", "application/json");
        
        // 设置请求体
        if (jsonBody != null && !jsonBody.isEmpty()) {
            StringEntity entity = new StringEntity(jsonBody, "UTF-8");
            httpPut.setEntity(entity);
        }
        
        try {
            HttpResponse response = httpClient.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
            
            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new Exception("Jira API Error: " + statusCode + " - " + responseBody);
            }
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
    
    /**
     * 执行 DELETE 请求
     */
    private String executeDelete(String url) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpDelete httpDelete = new HttpDelete(url);
        
        // 设置请求头
        httpDelete.setHeader("Authorization", authHeader);
        httpDelete.setHeader("Content-Type", "application/json");
        httpDelete.setHeader("Accept", "application/json");
        
        try {
            HttpResponse response = httpClient.execute(httpDelete);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
            
            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new Exception("Jira API Error: " + statusCode + " - " + responseBody);
            }
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
}

