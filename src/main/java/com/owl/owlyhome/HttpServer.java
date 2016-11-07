package com.owl.owlyhome;

import com.owl.owlyhome.video.VideoActor;
import com.owl.owlyhome.video.Video;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpCharsets;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Handler;
import akka.http.javadsl.server.Handler1;
import akka.http.javadsl.server.Handler2;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.RequestVal;
import static akka.http.javadsl.server.RequestVals.entityAs;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.values.PathMatcher;
import akka.http.javadsl.server.values.PathMatchers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.owl.owlyhome.video.Play;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import com.owl.owlyhome.video.V1Play;
import java.util.HashMap;

public class HttpServer extends HttpApp {

    private final ActorSystem system;
    private final ObjectMapper mapper = new ObjectMapper();
    private ActorRef videoActor = null;

    /////////////////////PI
    private String ROOT_FOLDER = "/mnt/usb";
    /////////////////////LOCAL
//    private String ROOT_FOLDER = "D:\\Download";
    private String IP_ADDRESS;

    HttpServer(ActorSystem system) {
        this.system = system;
        videoActor = system.actorOf(VideoActor.props(), "VideoActor");
    }

    void start() throws UnknownHostException, SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface n = interfaces.nextElement();
            Enumeration<InetAddress> addresses = n.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                System.out.println("Potential IP: " + n.getDisplayName() + " " + address.getHostAddress());
                String ip = address.getHostAddress();
                if (ip.startsWith("192.168.1")) {
                    IP_ADDRESS = ip;
                }
            }
        }
        System.out.println("Binding to IP:" + IP_ADDRESS);
        bindRoute(
                IP_ADDRESS,
                //IP.getHostAddress(),
                80,
                system
        );
        System.out.println("IP address bound!");
    }

    private final PathMatcher<String> htmlString = PathMatchers.regex("(.*html)");
    private final Handler1<String> htmlHandler = (ctx, htmlPath) -> {
        System.out.println("Request for html: " + htmlPath);
        return ctx.complete(ContentTypes.TEXT_HTML_UTF8,
                new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("web/" + htmlPath))).lines().collect(Collectors.joining("\n"))
        );
    };

    private final PathMatcher<String> cssString = PathMatchers.regex("(.*css)");
    private final Handler1<String> cssHandler = (ctx, cssPath) -> {
        System.out.println("Request for css: " + cssPath);
        return ctx.complete(ContentTypes.create(MediaTypes.TEXT_CSS, HttpCharsets.UTF_8),
                new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("web/" + cssPath))).lines().collect(Collectors.joining("\n"))
        );
    };

    private final PathMatcher<String> svgString = PathMatchers.regex("(.*svg)");
    private final Handler1<String> svgHandler = (ctx, svgPath) -> {
        System.out.println("Request for svg: " + svgPath);
        return ctx.complete(
                HttpResponse.create().withEntity(
                        HttpEntities.create(
                                ContentTypes.create(MediaTypes.IMAGE_SVG_XML),
                                new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("web/icons/" + svgPath))).lines().collect(Collectors.joining("\n")).getBytes()
                        )
                )
        );
    };

    private final Handler1<V1Play> playHandler = (ctx, request) -> {
        System.out.println("Request to play videoPath: " + request);
        videoActor.tell(new Play(request.filename, AudioOption.fromRestApi(request.audio)), ActorRef.noSender());
        return ctx.completeWithStatus(StatusCodes.OK);
    };

    private final Handler resumeHandler = (ctx) -> {
        System.out.println("Request to resume");
        videoActor.tell("resume", ActorRef.noSender());
        return ctx.completeWithStatus(StatusCodes.OK);
    };

    private final Handler pauseHandler = (ctx) -> {
        System.out.println("Request to pause");
        videoActor.tell("pause", ActorRef.noSender());
        return ctx.completeWithStatus(StatusCodes.OK);
    };

    private final Handler stopHandler = (ctx) -> {
        System.out.println("Request to stop");
        videoActor.tell("stop", ActorRef.noSender());
        return ctx.completeWithStatus(StatusCodes.OK);
    };

    private final RequestVal<V1Play> videoParam = entityAs(jsonAs(V1Play.class));

    @Override
    public Route createRoute() {
        return route(
                pathEndOrSingleSlash()
                .route(
                        getFromResource("web/index.html")
                ),
                path(htmlString)
                .route(
                        handleWith1(htmlString, htmlHandler)
                ),
                path(cssString)
                .route(
                        handleWith1(cssString, cssHandler)
                ),
                path("icons", svgString)
                .route(
                        handleWith1(svgString, svgHandler)
                ),
                get(
                        path("videos")
                        .route(
                                complete(getFiles())
                        )
                ),
                post(
                        path("play")
                        .route(
                                handleWith1(
                                        videoParam,
                                        playHandler
                                )
                        ),
                        path("resume")
                        .route(
                                handleWith(
                                        resumeHandler
                                )
                        ),
                        path("pause")
                        .route(
                                handleWith(
                                        pauseHandler
                                )
                        ),
                        path("stop")
                        .route(
                                handleWith(
                                        stopHandler
                                )
                        )
                )
        );
    }

    private String getFiles() {
        try {
            List<Video> files = walk(
                    ROOT_FOLDER,
                    new ArrayList<>());
            return mapper.writeValueAsString(files);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Video> walk(String path, List<Video> acc) {
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) {
            return acc;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                walk(f.getAbsolutePath(), acc);
            } else if (isSupportedFormat(f.getName())) {
                acc.add(new Video(f.getAbsolutePath(), f.getAbsolutePath(), f.getAbsolutePath()));
            }
        }
        return acc;
    }

    private boolean isSupportedFormat(String name) {
        if (name.endsWith(".avi") || name.endsWith(".mp4") || name.endsWith(".mkv")) {
            return true;
        }
        return false;
    }
}
