package com.ionos.go.plugin.notifier.message;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

/** This is an interface for a Go CD server message handler. */
public interface GoPluginApiRequestHandler {

    /**
     * Handles the request and returns a response.
     *
     * @param request the request to handle
     * @return the response
     */
    GoPluginApiResponse handle(final GoPluginApiRequest request);
}
