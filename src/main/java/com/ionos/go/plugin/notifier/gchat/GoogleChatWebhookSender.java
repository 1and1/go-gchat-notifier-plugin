package com.ionos.go.plugin.notifier.gchat;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.logging.Logger;
import lombok.NonNull;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

public class GoogleChatWebhookSender {
    private static final Logger LOGGER = Logger.getLoggerFor(GoogleChatWebhookSender.class);

    private String proxyUrl;

    public GoogleChatWebhookSender(String proxUrl) {
        this.proxyUrl =  proxUrl;
    }

    private String toJsonPayload(@NonNull String message) {
        GoogleChatRequest googleChatRequest = new GoogleChatRequest(message);
        final GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder.create().toJson(googleChatRequest);
    }

    private CloseableHttpClient newClient() {
        CloseableHttpClient result;

        if (proxyUrl != null) {
            HttpHost proxyHost = HttpHost.create(URI.create(proxyUrl));
            LOGGER.debug("Creating client with proxy " + proxyHost);
            result = HttpClients.custom().setProxy(proxyHost).build();
        } else {
            LOGGER.debug("Creating client with direct connection");
            result = HttpClients.createDefault();
        }
        return result;
    }

    public void send(@NonNull String url, @NonNull String message) throws IOException  {

        String jsonPayload = toJsonPayload(message);
        LOGGER.debug("JSON payload: " + message);

        try (CloseableHttpClient httpclient = newClient()) {
            ClassicHttpRequest httpPost = ClassicRequestBuilder.post(url)
                    .setEntity(jsonPayload)
                    .setHeader("Content-Type", "application/json; charset=UTF-8")
                    .build();

            CloseableHttpResponse response = httpclient.execute(httpPost);
            if (response.getCode() == 200) {
                LOGGER.debug("Response with HTTP code " + response.getCode() + " and HTTP reason " + response.getReasonPhrase());
            } else {
                LOGGER.warn("Response with HTTP code " + response.getCode() + " and HTTP reason " + response.getReasonPhrase());
            }

            final HttpEntity responseEntity = response.getEntity();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            responseEntity.writeTo(byteArrayOutputStream);
            String document = byteArrayOutputStream.toString("UTF-8");
            LOGGER.debug("Response document " + document);

            if (response.getCode() != 200) {
                throw new IOException("Google chat url returned http status " +
                        response.getCode() +
                        " " +
                        response.getReasonPhrase());
            }
        }
    }
}