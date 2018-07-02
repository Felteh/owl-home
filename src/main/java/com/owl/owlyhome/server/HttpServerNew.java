package com.owl.owlyhome.server;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;

public class HttpServerNew extends ErrorHandlingDirectives {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServerNew.class);
    private static final int PORT = 8080;

    private final ActorSystem system;
    private final ActorMaterializer materializer;
    private final Route[] routes;

    private ServerBinding boundRoute;

    public HttpServerNew(ActorSystem system, ActorMaterializer materializer, Route... routes) {
        this.system = system;
        this.materializer = materializer;
        this.routes = routes;
    }

    public void start() throws SocketException, InterruptedException, ExecutionException {
        String ipAddress = findUnboundIpAddress();

        LOG.debug("Binding");
        Http http = Http.get(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = createRoute().flow(system, materializer);
        boundRoute = http.bindAndHandle(routeFlow, ConnectHttp.toHost(ipAddress, PORT), materializer).toCompletableFuture().get();
        LOG.debug("Bound port={}", PORT);
    }
    private Route createRoute() {
        return route(
                path("app.js", () ->
                        get(() ->
                                getFromResource("web/app.js"))));
    }

    private Route createRouteX() {
        Route indexHtml = get(() -> route(pathSingleSlash(() -> getFromResource("web/index.html"))));
        Route appJsx = get(() -> route(pathPrefix("app.js", () -> getFromResource("web/app.js"))));
        Route pages = get(() -> route(pathPrefix("pages", () -> getFromResource("web/index.html"))));

        Route apis = route(
                appJsx,
                indexHtml,
                pages,
                route(
                        routes
                )
        );

        return logRequestResult(this::requestMethodAsInfo,
                this::rejectionsAsInfo,
                () -> handleExceptions(
                        exceptionHandlerLogAndReturnInternalError(),
                        () -> handleRejections(
                                rejectionHandlerLogAndReturnNotFound(),
                                () -> apis
                        )
                )
        );
    }

    private String findUnboundIpAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface n = interfaces.nextElement();
            Enumeration<InetAddress> addresses = n.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                LOG.debug("Potential IP: DisplayName={} HostAddress={}", n.getDisplayName(), address.getHostAddress());
                String ip = address.getHostAddress();
                if (ip.startsWith("192.168.1")) {
                    LOG.debug("IP Selected={}", ip);
                    return ip;
                }
            }
        }
        LOG.error("Could not find open interface - falling back to 127.0.0.1");
        return "127.0.0.1";
    }
}
