package com.ionos.go.plugin.notifier.gchat;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.logging.Logger;
import lombok.NonNull;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

/** Sends a message to the google chat service using a webhook url.
 * @see <a href="https://developers.google.com/chat/how-tos/webhooks">Googles howto on webhooks for GChat</a>
 * */
public class GoogleChatWebhookSender {
    private static final Logger LOGGER = Logger.getLoggerFor(GoogleChatWebhookSender.class);

    private final String proxyUrl;

    /** Creates a new instance.
     * @param proxyUrl optional proxy URL or {@code null}.
     * */
    public GoogleChatWebhookSender(String proxyUrl) {
        this.proxyUrl =  proxyUrl;
    }

    private String toJsonPayload(@NonNull String message) {
        GoogleChatRequest googleChatRequest = new GoogleChatRequest(message);
        final GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder.create().toJson(googleChatRequest);
    }

    /** Creates a new http client, honoring the proxy settings.
     * */
    private CloseableHttpClient newClient() {
        CloseableHttpClient result;

        if (proxyUrl != null && !proxyUrl.isEmpty()) {
            HttpHost proxyHost = HttpHost.create(URI.create(proxyUrl));
            LOGGER.debug("Creating client with proxy " + proxyHost);
            result = HttpClients.custom().setProxy(proxyHost).build();
        } else {
            LOGGER.debug("Creating client with direct connection");
            result = HttpClients.createDefault();
        }
        return result;
    }

    /** Send a chat message to the google chat service.
     * @param url the webhook url to send the message with, must be non {@code null}.
     * @param message the message to send, must be non {@code null}.
     * @throws IOException if I/O
     * */
    public void send(@NonNull String url, @NonNull String message) throws IOException  {

        String jsonPayload = toJsonPayload(message);
        LOGGER.debug("JSON payload: " + message);

        try (CloseableHttpClient httpclient = newClient()) {
            ClassicHttpRequest httpPost = ClassicRequestBuilder.post(url)
                    .setEntity(jsonPayload)
                    .setHeader("Content-Type", "application/json; charset=UTF-8")
                    .build();

            httpclient.execute(httpPost, classicHttpResponse -> {
                if (classicHttpResponse.getCode() == HttpStatus.SC_OK) {
                    LOGGER.debug("Response with HTTP code " + classicHttpResponse.getCode() + " and HTTP reason " + classicHttpResponse.getReasonPhrase());
                } else {
                    LOGGER.warn("Response with HTTP code " + classicHttpResponse.getCode() + " and HTTP reason " + classicHttpResponse.getReasonPhrase());
                }

                final HttpEntity responseEntity = classicHttpResponse.getEntity();

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                responseEntity.writeTo(byteArrayOutputStream);
                String document = byteArrayOutputStream.toString("UTF-8");
                LOGGER.debug("Response document " + document);

                if (classicHttpResponse.getCode() != HttpStatus.SC_OK) {
                    throw new IOException("Google chat url returned http status " +
                            classicHttpResponse.getCode() +
                            " " +
                            classicHttpResponse.getReasonPhrase());
                }
                return null;
            });
        }
    }
}
