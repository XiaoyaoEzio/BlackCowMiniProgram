package com.superyc.heiniu.utils;

import com.superyc.heiniu.exception.ErrorResponseCodeException;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Http工具类
 */
@Component
public class HttpUtils {
    private static OkHttpClient httpClient = new OkHttpClient();
    private static final MediaType MEDIA_TYPE_XML = MediaType.parse("text/xml;charset=utf-8");
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");

    public static String get(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        return execute(request);
    }

    public static String postXml(String url, String param) throws IOException {
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_XML, param);
        return post(url, requestBody);

    }

    public static String postJson(String url, String param) throws IOException {
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, param);
        return post(url, requestBody);
    }

    public static String postForm(String url, String paramKey, String paramValue) throws IOException {
        FormBody formBody = new FormBody.Builder().add(paramKey, paramValue).build();
        return post(url, formBody);
    }

    private static String post(String url, RequestBody requestBody) throws IOException {
        Request request = new Request.Builder().url(url).post(requestBody).build();
        return execute(request);
    }

    private static String execute(Request request) throws IOException {
        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new ErrorResponseCodeException("Unexpected code " + response);
        }

        return response.body() == null ? null : response.body().string();
    }


}
