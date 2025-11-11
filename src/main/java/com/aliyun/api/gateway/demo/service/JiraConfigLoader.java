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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Jira 配置加载器
 * 从配置文件或环境变量中加载 Jira 配置
 */
public class JiraConfigLoader {
    
    private static final String DEFAULT_CONFIG_FILE = "jira-config.properties";
    
    /**
     * 从默认配置文件加载配置
     * @return JiraConfig 对象
     * @throws IOException
     */
    public static JiraConfig loadFromFile() throws IOException {
        return loadFromFile(DEFAULT_CONFIG_FILE);
    }
    
    /**
     * 从指定配置文件加载配置
     * @param configFile 配置文件路径
     * @return JiraConfig 对象
     * @throws IOException
     */
    public static JiraConfig loadFromFile(String configFile) throws IOException {
        Properties props = new Properties();
        
        // 尝试从文件系统加载
        try (InputStream input = new FileInputStream(configFile)) {
            props.load(input);
        } catch (IOException e) {
            // 尝试从 classpath 加载
            try (InputStream input = JiraConfigLoader.class.getClassLoader()
                    .getResourceAsStream(configFile)) {
                if (input == null) {
                    throw new IOException("无法找到配置文件: " + configFile);
                }
                props.load(input);
            }
        }
        
        return createConfigFromProperties(props);
    }
    
    /**
     * 从环境变量加载配置
     * @return JiraConfig 对象
     */
    public static JiraConfig loadFromEnv() {
        String baseUrl = System.getenv("JIRA_BASE_URL");
        String username = System.getenv("JIRA_USERNAME");
        String apiToken = System.getenv("JIRA_API_TOKEN");
        String timeoutStr = System.getenv("JIRA_TIMEOUT");
        
        if (baseUrl == null || username == null || apiToken == null) {
            throw new IllegalStateException(
                "缺少必要的环境变量: JIRA_BASE_URL, JIRA_USERNAME, JIRA_API_TOKEN"
            );
        }
        
        int timeout = 30000; // 默认30秒
        if (timeoutStr != null && !timeoutStr.isEmpty()) {
            try {
                timeout = Integer.parseInt(timeoutStr);
            } catch (NumberFormatException e) {
                System.err.println("无效的超时时间配置，使用默认值: 30000ms");
            }
        }
        
        return new JiraConfig(baseUrl, username, apiToken, timeout);
    }
    
    /**
     * 从 Properties 对象创建 JiraConfig
     * @param props Properties 对象
     * @return JiraConfig 对象
     */
    private static JiraConfig createConfigFromProperties(Properties props) {
        String baseUrl = props.getProperty("jira.base.url");
        String username = props.getProperty("jira.username");
        String apiToken = props.getProperty("jira.api.token");
        String timeoutStr = props.getProperty("jira.timeout", "30000");
        
        if (baseUrl == null || username == null || apiToken == null) {
            throw new IllegalStateException(
                "配置文件缺少必要的属性: jira.base.url, jira.username, jira.api.token"
            );
        }
        
        int timeout = 30000;
        try {
            timeout = Integer.parseInt(timeoutStr);
        } catch (NumberFormatException e) {
            System.err.println("无效的超时时间配置，使用默认值: 30000ms");
        }
        
        return new JiraConfig(baseUrl, username, apiToken, timeout);
    }
    
    /**
     * 优先从环境变量加载，如果失败则从配置文件加载
     * @return JiraConfig 对象
     * @throws IOException
     */
    public static JiraConfig load() throws IOException {
        try {
            return loadFromEnv();
        } catch (IllegalStateException e) {
            System.out.println("从环境变量加载失败，尝试从配置文件加载...");
            return loadFromFile();
        }
    }
}

