package com.ionos.go.plugin.notifier.util;

import com.google.gson.Gson;
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

/** Miscellaneous helpers. */
public class Helper {
    private static final Logger LOGGER = Logger.getLoggerFor(Helper.class);

    private static final boolean DEBUG = false;

    private Helper() {
        // no instance
    }

    /** Dumps a String to a file in the tmp filesystem if
     * debugging is active.
     * Will catch and ignore all errors.
     * @param str the String to dump.
     *  */
    public static void debugDump(@NonNull String str) {
        if (! DEBUG) {
            return;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Path filename = Paths.get("/tmp", simpleDateFormat.format(new Date()) + ".txt");
        try (Writer writer = Files.newBufferedWriter(filename, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
            writer.write(str);
        } catch (IOException e) {
            LOGGER.warn("Could not write debug String", e);
        }
    }

    /** Dumps an Object to a JSON serialization in the tmp filesystem if
     * debugging is active.
     * Will catch and ignore all errors.
     * @param object the object to dump.
     *  */
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

    /** Reads a resource from the classpath.
     * @param resource the resource path in the classpath.
     * @return the loaded resource, decoded in UTF-8.
     * @throws IOException if loading the resource fails.
     * */
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
