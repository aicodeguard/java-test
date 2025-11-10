package com.aliyun.api.gateway.demo.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.aliyun.api.gateway.demo.constant.Constants;
import com.aliyun.api.gateway.demo.constant.HttpHeader;
import com.aliyun.api.gateway.demo.constant.SystemHeader;

/**
 * 签名工具类
 * 用于生成阿里云API网关请求签名
 */
public class SignUtil {

    private SignUtil() {
        // 工具类，禁止实例化
        throw new IllegalStateException("Utility class");
    }

    /**
     * 生成请求签名
     *
     * @param secret               密钥
     * @param method               HTTP方法
     * @param path                 请求路径
     * @param headers              请求头
     * @param querys               查询参数
     * @param bodys                请求体参数
     * @param signHeaderPrefixList 需要签名的请求头前缀列表
     * @return Base64编码的签名字符串
     * @throws RuntimeException 签名生成失败时抛出
     */
    public static String sign(String secret, String method, String path,
                               Map<String, String> headers,
                               Map<String, String> querys,
                               Map<String, String> bodys,
                               List<String> signHeaderPrefixList) {
        try {
            Mac hmacSha256 = Mac.getInstance(Constants.HMAC_SHA256);
            byte[] keyBytes = secret.getBytes(Constants.ENCODING);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, Constants.HMAC_SHA256);
            hmacSha256.init(secretKeySpec);

            String stringToSign = buildStringToSign(method, path, headers, querys, bodys, signHeaderPrefixList);
            byte[] signBytes = hmacSha256.doFinal(stringToSign.getBytes(Constants.ENCODING));
            return Base64.encodeBase64String(signBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error while generating signature: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during signature generation", e);
        }
    }

    /**
     * 构建待签名字符串
     */
    private static String buildStringToSign(String method, String path,
                                             Map<String, String> headers,
                                             Map<String, String> querys,
                                             Map<String, String> bodys,
                                             List<String> signHeaderPrefixList) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.toUpperCase()).append(Constants.LF);

        if (headers != null) {
            sb.append(getHeaderOrEmpty(headers, HttpHeader.HTTP_HEADER_ACCEPT))
                    .append(Constants.LF)
                    .append(getHeaderOrEmpty(headers, HttpHeader.HTTP_HEADER_CONTENT_MD5))
                    .append(Constants.LF)
                    .append(getHeaderOrEmpty(headers, HttpHeader.HTTP_HEADER_CONTENT_TYPE))
                    .append(Constants.LF)
                    .append(getHeaderOrEmpty(headers, HttpHeader.HTTP_HEADER_DATE))
                    .append(Constants.LF);
        } else {
            // headers为null时，添加4个换行符
            sb.append(Constants.LF).append(Constants.LF)
                    .append(Constants.LF).append(Constants.LF);
        }

        sb.append(buildHeaders(headers, signHeaderPrefixList))
                .append(buildResource(path, querys, bodys));

        return sb.toString();
    }

    /**
     * 构建资源路径字符串（包含查询参数和body参数）
     */
    private static String buildResource(String path, Map<String, String> querys, Map<String, String> bodys) {
        StringBuilder sb = new StringBuilder(path != null ? path : "");
        Map<String, String> sortMap = new TreeMap<>();

        if (querys != null) {
            sortMap.putAll(querys);
        }
        if (bodys != null) {
            sortMap.putAll(bodys);
        }

        if (sortMap.isEmpty()) {
            return sb.toString();
        }

        String paramString = sortMap.entrySet().stream()
                .filter(entry -> !StringUtils.isBlank(entry.getKey()))
                .map(entry -> {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    return StringUtils.isBlank(value) ? key : key + Constants.SPE4 + value;
                })
                .collect(Collectors.joining(Constants.SPE3));

        if (!paramString.isEmpty()) {
            sb.append(Constants.SPE5).append(paramString);
        }

        return sb.toString();
    }

    /**
     * 构建需要签名的请求头字符串
     */
    private static String buildHeaders(Map<String, String> headers, List<String> signHeaderPrefixList) {
        if (headers == null || signHeaderPrefixList == null) {
            return "";
        }

        // 过滤掉不需要的请求头前缀
        List<String> filteredPrefixes = new ArrayList<>(signHeaderPrefixList);
        filteredPrefixes.removeAll(Arrays.asList(
                SystemHeader.X_CA_SIGNATURE,
                HttpHeader.HTTP_HEADER_ACCEPT,
                HttpHeader.HTTP_HEADER_CONTENT_MD5,
                HttpHeader.HTTP_HEADER_CONTENT_TYPE,
                HttpHeader.HTTP_HEADER_DATE
        ));
        Collections.sort(filteredPrefixes);

        Map<String, String> sortedHeaders = new TreeMap<>(headers);
        StringBuilder sb = new StringBuilder();
        StringBuilder signHeaders = new StringBuilder();

        for (Map.Entry<String, String> entry : sortedHeaders.entrySet()) {
            String headerName = entry.getKey();
            if (isHeaderToSign(headerName, filteredPrefixes)) {
                String headerValue = entry.getValue() != null ? entry.getValue() : "";
                sb.append(headerName)
                        .append(Constants.SPE2)
                        .append(headerValue)
                        .append(Constants.LF);

                if (signHeaders.length() > 0) {
                    signHeaders.append(Constants.SPE1);
                }
                signHeaders.append(headerName);
            }
        }

        headers.put(SystemHeader.X_CA_SIGNATURE_HEADERS, signHeaders.toString());
        return sb.toString();
    }

    /**
     * 判断请求头是否需要参与签名
     */
    private static boolean isHeaderToSign(String headerName, List<String> signHeaderPrefixList) {
        if (StringUtils.isBlank(headerName)) {
            return false;
        }

        // 系统请求头或匹配前缀列表的请求头需要签名
        return headerName.startsWith(Constants.CA_HEADER_TO_SIGN_PREFIX_SYSTEM) ||
                signHeaderPrefixList.stream().anyMatch(headerName::equalsIgnoreCase);
    }

    /**
     * 获取请求头的值，不存在时返回空字符串
     */
    private static String getHeaderOrEmpty(Map<String, String> headers, String key) {
        return headers.getOrDefault(key, "");
    }
}