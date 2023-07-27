package com.ionos.go.plugin.notifier;

import lombok.NonNull;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletHandler;

import javax.servlet.Servlet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** An embedded server that serves resources from a filesystem path. */
public class EmbeddedHttpServer {

    private final Server server;

    /** Can be null, waiters will get notified over the monitor below. */
    private Integer runningPort;

    private final Object monitor;

    private final Runnable serverRunnable;

    private ExecutorService executorService;

    private HandlerList resourceHandler;

    private final ServletHandler servletHandler;

    /** Creates a new instance.
     * */
    public EmbeddedHttpServer() {
        monitor = new Object();
        server = new Server();

        final ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(0);
        server.addConnector(serverConnector);
        this.servletHandler = new ServletHandler();
        server.setHandler(servletHandler);

        serverRunnable = () -> {
            try {
                server.start();
                runningPort = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
                synchronized (monitor) {
                    monitor.notifyAll();
                }
                server.join();
            } catch (final Exception e) {
                throw new IllegalStateException("Could not initialize server", e);
            }
        };
    }

    public EmbeddedHttpServer withServlet(@NonNull final Class<? extends Servlet> servletClass, String mapping) {
        servletHandler.addServletWithMapping(servletClass, mapping);
        return this;
    }

    public int getRunningPort() {
        synchronized(monitor) {
            while (runningPort == null) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Should not get interrupted");
                }
            }
        }
        return runningPort;
    }

    public void start() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(serverRunnable);
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            // ignore
        }
        executorService.shutdown();
    }
}
