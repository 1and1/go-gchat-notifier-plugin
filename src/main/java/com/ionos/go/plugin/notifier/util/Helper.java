package com.ionos.go.plugin.notifier.util;

import com.google.gson.Gson;
import com.ionos.go.plugin.notifier.message.incoming.StageStatusRequest;
import com.thoughtworks.go.plugin.api.logging.Logger;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Helper {
    private static final Logger LOGGER = Logger.getLoggerFor(Helper.class);

    private static final boolean DEBUG = false;

    public static void debugDump(@NonNull String str) {
        if (! DEBUG) {
            return;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Path filename = Paths.get("/tmp", simpleDateFormat.format(new Date()) + ".txt");
        Gson gson = new Gson();
        try (Writer writer = Files.newBufferedWriter(filename, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
            writer.write(str);
        } catch (IOException e) {
            LOGGER.warn("Could not write debug String", e);
        }
    }

    public static void debugDump(@NonNull Object object) {
        if (! DEBUG) {
            return;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Path filename = Paths.get("/tmp", simpleDateFormat.format(new Date()) + ".json");
        try (Writer writer = Files.newBufferedWriter(filename, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
            writer.write(JsonUtil.toJsonString(object));
        } catch (IOException e) {
            LOGGER.warn("Could not write debug JSON", e);
        }
    }

    public static String readResource(String resource) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(Helper.class.getResourceAsStream(resource)), StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[256];
            int length;
            while ((length = reader.read(buffer)) >= 0) {
                sb.append(buffer, 0, length);
            }
            LOGGER.debug("Read resource with length " + sb.length());
            return sb.toString();
        }
    }
}
