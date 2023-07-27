package com.ionos.go.plugin.notifier;

import com.thoughtworks.go.plugin.api.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.apache.hc.core5.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;

public class GoogleMockServlet extends HttpServlet {

    @Getter
    private static String capturedRequestContentType;

    @Getter
    private static String capturedRequestBody;

    @Getter
    private static int invocations;

    @Setter
    private static int statusToReturn;

    static void reset() {
        invocations = 0;
        statusToReturn = HttpStatus.SC_OK;
        capturedRequestBody = null;
        capturedRequestContentType = null;
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Logger.getLoggerFor(GoNotifierPluginStageStatusTest.class).debug("Got a POST request");
        invocations++;
        capturedRequestContentType = req.getHeader("Content-Type");
        BufferedReader reader = req.getReader();
        StringWriter writer = new StringWriter();
        reader.lines().forEach(writer::append);
        capturedRequestBody = writer.toString();
        resp.setStatus(statusToReturn);
    }
}
