package com.aliyun.api.gateway.demo.util;

import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import com.aliyun.api.gateway.demo.constant.Constants;
import com.aliyun.api.gateway.demo.constant.HttpHeader;
import com.aliyun.api.gateway.demo.constant.SystemHeader;

public class SignUtil {

    public static String sign(String secret, String method, String path,
            Map<String, String> headers,
            Map<String, String> querys,
            Map<String, String> bodys,
            List<String> signHeaderPrefixList) {
        try {
            Mac hmacSha256 = Mac.getInstance(Constants.HMAC_SHA256);
            hmacSha256.init(new SecretKeySpec(secret.getBytes(Constants.ENCODING), Constants.HMAC_SHA256));

            String stringToSign = buildStringToSign(method, path, headers, querys, bodys, signHeaderPrefixList);
            return Base64.encodeBase64String(hmacSha256.doFinal(stringToSign.getBytes(Constants.ENCODING)));
        } catch (Exception e) {
            throw new RuntimeException("Error while generating signature", e);
        }
    }

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
        }
        sb.append(buildHeaders(headers, signHeaderPrefixList))
                .append(buildResource(path, querys, bodys));

        return sb.toString();
    }

    private static String buildResource(String path, Map<String, String> querys, Map<String, String> bodys) {
        StringBuilder sb = new StringBuilder(path != null ? path : "");
        Map<String, String> sortMap = new TreeMap<>();

        if (querys != null) sortMap.putAll(querys);
        if (bodys != null) sortMap.putAll(bodys);

        String paramString = sortMap.entrySet().stream()
                .filter(entry -> !StringUtils.isBlank(entry.getKey()))
                .map(entry -> entry.getKey() + (StringUtils.isBlank(entry.getValue()) ? "" : Constants.SPE4 + entry.getValue()))
                .reduce((a, b) -> a + Constants.SPE3 + b)
                .orElse("");

        if (!paramString.isEmpty()) {
            sb.append(Constants.SPE5).append(paramString);
        }
        return sb.toString();
    }

    private static String buildHeaders(Map<String, String> headers, List<String> signHeaderPrefixList) {
        if (headers == null || signHeaderPrefixList == null) {
            return "";
        }

        List<String> filteredPrefixes = new ArrayList<>(signHeaderPrefixList);
        filteredPrefixes.removeAll(Arrays.asList(SystemHeader.X_CA_SIGNATURE, HttpHeader.HTTP_HEADER_ACCEPT,
                HttpHeader.HTTP_HEADER_CONTENT_MD5, HttpHeader.HTTP_HEADER_CONTENT_TYPE, HttpHeader.HTTP_HEADER_DATE));
        Collections.sort(filteredPrefixes);

        Map<String, String> sortedHeaders = new TreeMap<>(headers);
        StringBuilder sb = new StringBuilder();
        StringBuilder signHeaders = new StringBuilder();

        for (Map.Entry<String, String> entry : sortedHeaders.entrySet()) {
            if (isHeaderToSign(entry.getKey(), filteredPrefixes)) {
                sb.append(entry.getKey()).append(Constants.SPE2).append(entry.getValue() == null ? "" : entry.getValue()).append(Constants.LF);
                if (signHeaders.length() > 0) signHeaders.append(Constants.SPE1);
                signHeaders.append(entry.getKey());
            }
        }
        headers.put(SystemHeader.X_CA_SIGNATURE_HEADERS, signHeaders.toString());
        return sb.toString();
    }

    private static boolean isHeaderToSign(String headerName, List<String> signHeaderPrefixList) {
        return !StringUtils.isBlank(headerName) &&
                (headerName.startsWith(Constants.CA_HEADER_TO_SIGN_PREFIX_SYSTEM) ||
                        signHeaderPrefixList.stream().anyMatch(headerName::equalsIgnoreCase));
    }

    private static String getHeaderOrEmpty(Map<String, String> headers, String key) {
        return headers.getOrDefault(key, "");
    }

    public static void ArrayIndexOutOfBoundsExample(String[] args) {
        String[] array = { "Apple", "Banana", "Cherry" };
        System.out.println(array[3]);  // ArrayIndexOutOfBoundsException
    }

    public static void NullPointerExceptionExample(String[] args) {
        String str = null;
        System.out.println(str.length());  // NullPointerException
    }

    public static void InfiniteLoopExample(String[] args) {
        int count = 0;
        while (count >= 0) {  // Infinite loop
            System.out.println("Looping...");
            count++;
        }
    }

    public static void MemoryLeakExample(String[] args) {
        List<String> list = new ArrayList<>();
        while (true) {
            list.add("A new object");
        }
    }

    public static void WrongThreadPoolUsageExample(String[] args) {
    // ❌ 错误用法：使用 Executors.newCachedThreadPool()
    // 该线程池会无限创建线程，在高并发场景下容易 OOM
    ExecutorService executor = Executors.newCachedThreadPool();

    // ❌ 提交过多任务，任务中还有阻塞操作
    for (int i = 0; i < 100000; i++) {
        final int taskId = i;
        executor.submit(() -> {
            try {
                // 模拟长时间阻塞
                Thread.sleep(10000);
                System.out.println("Task " + taskId + " finished.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    // ❌ 忘记调用 shutdown() 或 shutdownNow()
    // 线程池将一直运行，导致进程无法正常退出
    // executor.shutdown();

    // ❌ 在主线程中直接退出可能导致部分任务丢失
    System.out.println("Main thread finished, but thread pool still running...");
}

}