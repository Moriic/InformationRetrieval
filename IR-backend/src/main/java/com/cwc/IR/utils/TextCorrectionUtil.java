package com.cwc.IR.utils;


import com.cwc.IR.model.CorrectionResponse;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 文本纠错 WebAPI 接口调用示例
 * 运行前：请先填写Appid、APIKey、APISecret以及图片路径
 * 运行方法：直接运行 main() 即可
 * 结果： 控制台输出结果信息
 * 接口文档（必看）：<a href="https://www.xfyun.cn/doc/nlp/textCorrection/API.html">...</a>
 * uid与res_id可以到ResIdGet上传和获取
 *
 * @author iflytek
 */
@Slf4j
public class TextCorrectionUtil {
    // 地址与鉴权信息
    public static final String hostUrl = "https://api.xf-yun.com/v1/private/s9a87e3ec";
    public static final String appid = "68bec4d3";
    public static final String apiSecret = "N2FhNmIxZGRmZjlmYjFhZGZhYTY0NDI2";
    public static final String apiKey = "c10c9ce986fef5d0e048d97b360e94b0";
    // json
    public static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        correctQuery("青花");
    }
    // 对query进行纠错
    public static List<String> correctQuery(String query) throws Exception {
        String url = getAuthUrl(hostUrl, apiKey, apiSecret);
        String json = getRequestJson(query);
        String backResult = doPostJson(url, json);
        JsonParse jsonParse = gson.fromJson(backResult, JsonParse.class);
        String base64Decode = new String(Base64.getDecoder().decode(jsonParse.payload.result.text), StandardCharsets.UTF_8);
        log.info("text字段base64解码后纠错信息：{}", base64Decode);
        CorrectionResponse correctionResponse = gson.fromJson(base64Decode, CorrectionResponse.class);

        // 存储不同纠错结果的集合
        Set<String> correctedQueries = new HashSet<>();
        List<String> correctedResults = new ArrayList<>();

        // 使用不同的纠错类型进行修改
        String correctedQuery = applyCombinedCorrections(query, correctionResponse);
        if (!correctedQuery.equals(query)) {
            correctedQueries.add(correctedQuery);
            correctedResults.add(correctedQuery);
        }

        correctedQuery = applyCorrections(query, correctionResponse.getChar1());
        if (!correctedQuery.equals(query) && !correctedQueries.contains(correctedQuery)) {
            correctedQueries.add(correctedQuery);
            correctedResults.add(correctedQuery);
        }
        correctedQuery = applyCorrections(query, correctionResponse.getWord());
        if (!correctedQuery.equals(query) && !correctedQueries.contains(correctedQuery)) {
            correctedQueries.add(correctedQuery);
            correctedResults.add(correctedQuery);
        }
        correctedQuery = applyCorrections(query, correctionResponse.getIdm());
        if (!correctedQuery.equals(query) && !correctedQueries.contains(correctedQuery)) {
            correctedQueries.add(correctedQuery);
            correctedResults.add(correctedQuery);
        }
        log.info("纠错结果：{}", correctedQueries);
        return correctedResults;
    }

    // 应用单一类型的纠错
    private static String applyCorrections(String originalQuery, List<List<Object>> corrections) {
        if (corrections == null) {
            return originalQuery;
        }
        String correctedQuery = originalQuery;

        for (List<Object> correction : corrections) {
            if (correction.size() != 4) continue;
            int pos = ((Double) correction.get(0)).intValue();
            String cur = (String) correction.get(1);
            String correct = (String) correction.get(2);
            if (!correct.isEmpty()) {
                if (cur.length() == 1) {
                    correctedQuery = replaceCharAt(correctedQuery, pos, correct.charAt(0));
                } else {
                    correctedQuery = replaceWordAt(correctedQuery, pos, cur.length(), correct);
                }
            }
        }
        return correctedQuery;
    }

    // 应用组合类型的纠错
    private static String applyCombinedCorrections(String originalQuery, CorrectionResponse correctionResponse) {
        String combinedCorrectedQuery = applyCorrections(originalQuery, correctionResponse.getChar1());
        combinedCorrectedQuery = applyCorrections(combinedCorrectedQuery, correctionResponse.getWord());
        combinedCorrectedQuery = applyCorrections(combinedCorrectedQuery, correctionResponse.getIdm());
        return combinedCorrectedQuery;
    }

    // 辅助函数：替换字符串中特定位置的字符
    private static String replaceCharAt(String str, int pos, char replacement) {
        return str.substring(0, pos) + replacement + str.substring(pos + 1);
    }

    // 辅助函数：替换字符串中特定位置的单词或成语
    private static String replaceWordAt(String str, int pos, int length, String replacement) {
        return str.substring(0, pos) + replacement + str.substring(pos + length);
    }

    // 请求参数json拼接
    public static String getRequestJson(String text) {
        return "{\n" +
                "  \"header\": {\n" +
                "    \"app_id\": \"" + appid + "\",\n" +
                //"    \"uid\": \"XXXXX\",\n" +
                "    \"status\": 3\n" +
                "  },\n" +
                "  \"parameter\": {\n" +
                "    \"s9a87e3ec\": {\n" +
                //"    \"res_id\": \"XXXXX\",\n" +
                "      \"result\": {\n" +
                "        \"encoding\": \"utf8\",\n" +
                "        \"compress\": \"raw\",\n" +
                "        \"format\": \"json\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"payload\": {\n" +
                "    \"input\": {\n" +
                "      \"encoding\": \"utf8\",\n" +
                "      \"compress\": \"raw\",\n" +
                "      \"format\": \"plain\",\n" +
                "      \"status\": 3,\n" +
                "      \"text\": \"" + getBase64TextData(text) + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    // 读取文件
    public static String getBase64TextData(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes());
    }

    // 根据json和url发起post请求
    public static String doPostJson(String url, String json) {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        CloseableHttpResponse closeableHttpResponse = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            // 执行http请求
            closeableHttpResponse = closeableHttpClient.execute(httpPost);
            resultString = EntityUtils.toString(closeableHttpResponse.getEntity(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (closeableHttpResponse != null) {
                    closeableHttpResponse.close();
                }
                if (closeableHttpClient != null) {
                    closeableHttpClient.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }

    // 鉴权方法
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "POST " + url.getPath() + " HTTP/1.1";
        //System.out.println(preStr);
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        return httpUrl.toString();
    }

    //返回的json结果拆解
    static class JsonParse {
        Payload payload;
    }

    static class Payload {
        Result result;
    }

    static class Result {
        String text;
    }
}
