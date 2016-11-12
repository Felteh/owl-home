package com.owl.owlyhome.video;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.owl.owlyhome.AudioOption;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class VideoRoute extends AllDirectives implements Supplier<Route> {

    private static final Logger LOG = LoggerFactory.getLogger(VideoRoute.class);

    private FiniteDuration DEFAULT_DURATION = Duration.apply(10, TimeUnit.SECONDS);
    private String ROOT_FOLDER = "D:\\Download";
    private final ObjectMapper mapper;
    private final ActorRef videoActor;

    public VideoRoute(ActorSystem system, ObjectMapper mapper) {
        this.mapper = mapper;
        videoActor = system.actorOf(VideoActor.props(), "VideoActor");
    }

    @Override
    public Route get() {
        return route(
                get(
                        () -> route(
                                path(
                                        "videos",
                                        () -> this.complete(getFiles())
                                ),
                                path(
                                        "resume",
                                        () -> this.completeWithFutureStatus(resume())
                                ),
                                path(
                                        "pause",
                                        () -> this.completeWithFutureStatus(pause())
                                ),
                                path(
                                        "stop",
                                        () -> this.completeWithFutureStatus(stop())
                                )
                        )
                ),
                post(
                        () -> route(
                                path(
                                        "play",
                                        () -> entity(
                                                Jackson.unmarshaller(V1Play.class),
                                                (t) -> this.completeWithFutureStatus(play(t))
                                        )
                                )
                        )
                )
        );
    }

    private StatusCode resolveFutureToStatusCode(Object res) {
        try {
            LOG.debug("resolveFutureToStatusCode Result={}", res);
            if (VideoActor.SUCCESS.equals(res)) {
                return StatusCodes.OK;
            }
            if (VideoActor.FAILURE.equals(res)) {
                return StatusCodes.BAD_REQUEST;
            }
            if (VideoActor.ERROR.equals(res)) {
                return StatusCodes.INTERNAL_SERVER_ERROR;
            }
            throw new Exception("Return type of actors peculiar:" + res);
        } catch (Exception e) {
            LOG.error("Error", e);
            return StatusCodes.INTERNAL_SERVER_ERROR;
        }
    }

    private CompletionStage<StatusCode> play(V1Play request) {
        CompletionStage<Object> ask = PatternsCS.ask(videoActor, new Play(request.filename, AudioOption.fromRestApi(request.audio)), Timeout.apply(DEFAULT_DURATION));
        return ask.thenApply(this::resolveFutureToStatusCode);
    }

    private CompletionStage<StatusCode> resume() {
        CompletionStage<Object> ask = PatternsCS.ask(videoActor, VideoActor.RESUME, Timeout.apply(DEFAULT_DURATION));
        return ask.thenApply(this::resolveFutureToStatusCode);
    }

    private CompletionStage<StatusCode> pause() {
        CompletionStage<Object> ask = PatternsCS.ask(videoActor, VideoActor.PAUSE, Timeout.apply(DEFAULT_DURATION));
        return ask.thenApply(this::resolveFutureToStatusCode);
    }

    private CompletionStage<StatusCode> stop() {
        CompletionStage<Object> ask = PatternsCS.ask(videoActor, VideoActor.STOP, Timeout.apply(DEFAULT_DURATION));
        return ask.thenApply(this::resolveFutureToStatusCode);
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
                acc.add(new Video(f.getAbsolutePath(), f.getName(), formatAsMegabytes(f.length())));
            }
        }
        return acc;
    }

    private boolean isSupportedFormat(String name) {
        return name.endsWith(".avi") || name.endsWith(".mp4") || name.endsWith(".mkv");
    }

    private String formatAsMegabytes(long bytes) {
        double bytesD = bytes;
        return String.format("%.2f", bytesD / 1024d / 1024d);
    }
}
