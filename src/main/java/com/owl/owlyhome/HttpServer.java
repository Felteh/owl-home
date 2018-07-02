//package com.owl.owlyhome;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
//import akka.http.javadsl.model.ContentTypes;
//import akka.http.javadsl.model.HttpCharsets;
//import akka.http.javadsl.model.HttpEntities;
//import akka.http.javadsl.model.HttpResponse;
//import akka.http.javadsl.model.MediaTypes;
//import akka.http.javadsl.model.StatusCode;
//import akka.http.javadsl.model.StatusCodes;
//import akka.http.javadsl.server.Handler;
//import akka.http.javadsl.server.Handler1;
//import akka.http.javadsl.server.HttpApp;
//import akka.http.javadsl.server.RequestContext;
//import akka.http.javadsl.server.RequestVal;
//import static akka.http.javadsl.server.RequestVals.entityAs;
//import akka.http.javadsl.server.Route;
//import akka.http.javadsl.server.RouteResult;
//import akka.http.javadsl.server.values.PathMatcher;
//import akka.http.javadsl.server.values.PathMatchers;
//import akka.pattern.Patterns;
//import akka.util.Timeout;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.owl.owlyhome.video.Play;
//import com.owl.owlyhome.video.V1Play;
//import com.owl.owlyhome.video.Video;
//import com.owl.owlyhome.video.VideoActor;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.InputStreamReader;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.SocketException;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//import scala.concurrent.Await;
//import scala.concurrent.Future;
//import scala.concurrent.duration.Duration;
//import scala.concurrent.duration.FiniteDuration;
//
//public class HttpServer extends HttpApp {
//
//    private FiniteDuration DEFAULT_DURATION = Duration.apply(10, TimeUnit.SECONDS);
//
//    private final ActorSystem system;
//    private final ObjectMapper mapper = new ObjectMapper();
//    private ActorRef videoActor = null;
//
//    /////////////////////PI
//    private String ROOT_FOLDER = "/mnt/usb";
////    private String ROOT_FOLDER = "/Users/dstoner/PersonalRepos/owl-home";
//    /////////////////////LOCAL
////        private String ROOT_FOLDER = "D:\\Download";
//    private String IP_ADDRESS;
//    private int PORT = 80;
//
//    HttpServer(ActorSystem system) {
//        this.system = system;
//        videoActor = system.actorOf(VideoActor.props(), "VideoActor");
//    }
//
//    private String formatAsMegabytes(long bytes) {
//        double bytesD = bytes;
//        return String.format("%.2f", bytesD / 1024d / 1024d);
//    }
//
//    private StatusCode resolveFutureToStatusCode(Future<Object> ask) {
//        try {
//            Object res = Await.result(ask, DEFAULT_DURATION);
//            System.out.println("Result:" + res);
//            if (VideoActor.SUCCESS.equals(res)) {
//                return StatusCodes.OK;
//            }
//            if (VideoActor.FAILURE.equals(res)) {
//                return StatusCodes.BAD_REQUEST;
//            }
//            if (VideoActor.ERROR.equals(res)) {
//                return StatusCodes.INTERNAL_SERVER_ERROR;
//            }
//            throw new Exception("Return type of actors peculiar:" + res);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return StatusCodes.INTERNAL_SERVER_ERROR;
//        }
//    }
//
//    void start() throws UnknownHostException, SocketException {
//        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//        while (interfaces.hasMoreElements()) {
//            NetworkInterface n = interfaces.nextElement();
//            Enumeration<InetAddress> addresses = n.getInetAddresses();
//            while (addresses.hasMoreElements()) {
//                InetAddress address = addresses.nextElement();
//                System.out.println("Potential IP: " + n.getDisplayName() + " " + address.getHostAddress());
//                String ip = address.getHostAddress();
//                if (ip.startsWith("192.168.1")) {
//                    IP_ADDRESS = ip;
//                }
//            }
//        }
//        System.out.println("Binding to IP:" + IP_ADDRESS);
//        bindRoute(
//                IP_ADDRESS,
//                PORT,
//                system
//        );
//        System.out.println("IP address bound!");
//    }
//
//    private final PathMatcher<String> htmlString = PathMatchers.regex("(.*html)");
//    private final Handler1<String> htmlHandler = (ctx, htmlPath) -> {
//        System.out.println("Request for html: " + htmlPath);
//        return ctx.complete(ContentTypes.TEXT_HTML_UTF8,
//                            new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("static/" + htmlPath))).lines().collect(Collectors.joining("\n"))
//        );
//    };
//
//    private final PathMatcher<String> cssString = PathMatchers.regex("(.*css)");
//    private final Handler1<String> cssHandler = (ctx, cssPath) -> {
//        System.out.println("Request for css: " + cssPath);
//        return ctx.complete(ContentTypes.create(MediaTypes.TEXT_CSS, HttpCharsets.UTF_8),
//                            new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("static/" + cssPath))).lines().collect(Collectors.joining("\n"))
//        );
//    };
//
//    private final PathMatcher<String> svgString = PathMatchers.regex("(.*svg)");
//    private final Handler1<String> svgHandler = (ctx, svgPath) -> {
//        System.out.println("Request for svg: " + svgPath);
//        return ctx.complete(
//                HttpResponse.create().withEntity(
//                        HttpEntities.create(
//                                ContentTypes.create(MediaTypes.IMAGE_SVG_XML),
//                                new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("static/icons/" + svgPath))).lines().collect(Collectors.joining("\n")).getBytes()
//                        )
//                )
//        );
//    };
//
//    private final Handler1<V1Play> playHandler = (ctx, request) -> {
//        System.out.println("Request to play videoPath: " + request);
//        Future<Object> ask = Patterns.ask(videoActor, new Play(request.filename, AudioOption.fromRestApi(request.audio)), Timeout.apply(DEFAULT_DURATION));
//        return ctx.completeWithStatus(resolveFutureToStatusCode(ask));
//    };
//
//    private final Handler resumeHandler = (ctx) -> {
//        System.out.println("Request to resume");
//        Future<Object> ask = Patterns.ask(videoActor, VideoActor.RESUME, Timeout.apply(DEFAULT_DURATION));
//        return ctx.completeWithStatus(resolveFutureToStatusCode(ask));
//    };
//
//    private final Handler pauseHandler = (ctx) -> {
//        System.out.println("Request to pause");
//        Future<Object> ask = Patterns.ask(videoActor, VideoActor.PAUSE, Timeout.apply(DEFAULT_DURATION));
//        return ctx.completeWithStatus(resolveFutureToStatusCode(ask));
//    };
//
//    private final Handler stopHandler = (ctx) -> {
//        System.out.println("Request to stop");
//        Future<Object> ask = Patterns.ask(videoActor, VideoActor.STOP, Timeout.apply(DEFAULT_DURATION));
//        return ctx.completeWithStatus(resolveFutureToStatusCode(ask));
//    };
//
//    private final RequestVal<V1Play> videoParam = entityAs(jsonAs(V1Play.class));
//
//    @Override
//    public Route createRoute() {
//        return route(
//                pathEndOrSingleSlash()
//                .route(
//                        getFromResource("static/index.html")
//                ),
//                path(htmlString)
//                .route(
//                        handleWith1(htmlString, htmlHandler)
//                ),
//                path(cssString)
//                .route(
//                        handleWith1(cssString, cssHandler)
//                ),
//                path("icons", svgString)
//                .route(
//                        handleWith1(svgString, svgHandler)
//                ),
//                get(
//                        path("videos")
//                        .route(
//                                handleWith(this::getFiles)
//                        )
//                ),
//                post(
//                        path("play")
//                        .route(
//                                handleWith1(
//                                        videoParam,
//                                        playHandler
//                                )
//                        ),
//                        path("resume")
//                        .route(
//                                handleWith(
//                                        resumeHandler
//                                )
//                        ),
//                        path("pause")
//                        .route(
//                                handleWith(
//                                        pauseHandler
//                                )
//                        ),
//                        path("stop")
//                        .route(
//                                handleWith(
//                                        stopHandler
//                                )
//                        )
//                )
//        );
//    }
//
//    private RouteResult getFiles(RequestContext ctx) {
//        try {
//            List<Video> files = walk(
//                    ROOT_FOLDER,
//                    new ArrayList<>());
//            return ctx.complete(mapper.writeValueAsString(files));
//        } catch (JsonProcessingException ex) {
//            throw new RuntimeException(ex);
//        }
//    }
//
//    public List<Video> walk(String path, List<Video> acc) {
//        File root = new File(path);
//        File[] list = root.listFiles();
//
//        if (list == null) {
//            return acc;
//        }
//
//        for (File f : list) {
//            if (f.isDirectory()) {
//                walk(f.getAbsolutePath(), acc);
//            } else if (isSupportedFormat(f.getName())) {
//                acc.add(new Video(f.getAbsolutePath(), f.getName(), formatAsMegabytes(f.length())));
//            }
//        }
//        return acc;
//    }
//
//    private boolean isSupportedFormat(String name) {
//        if (name.endsWith(".avi") || name.endsWith(".mp4") || name.endsWith(".mkv")) {
//            return true;
//        }
//        return false;
//    }
//}
