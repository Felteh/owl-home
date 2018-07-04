package com.owl.owlyhome.video;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.util.Timeout;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.owl.owlyhome.AudioOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VideoRoute extends AllDirectives implements Supplier<Route> {

    private static final Logger LOG = LoggerFactory.getLogger(VideoRoute.class);

    private FiniteDuration DEFAULT_DURATION = Duration.apply(10, TimeUnit.SECONDS);
    //    private String ROOT_FOLDER = "/mnt/usb";
    private String ROOT_FOLDER = "/Users/dstoner/MercurialRepos/Other/owl-home/example-movs";
    private final ObjectMapper mapper;
    private final ActorSystem<TypedVideoActor.VideoCommand> system;

    public VideoRoute(ActorSystem<TypedVideoActor.VideoCommand> system, ObjectMapper mapper) {
        this.mapper = mapper;
        this.system = system;
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
                                path(PathMatchers.segment("videos").slash("resume"),
                                        () -> this.completeWithFutureStatus(resume())
                                ),
                                path(
                                        PathMatchers.segment("videos").slash("pause"),
                                        () -> this.completeWithFutureStatus(pause())
                                ),
                                path(
                                        PathMatchers.segment("videos").slash("stop"),
                                        () -> this.completeWithFutureStatus(stop())
                                )
                        )
                ),
                post(
                        () -> route(
                                path(
                                        PathMatchers.segment("videos").slash("play"),
                                        () -> entity(
                                                Jackson.unmarshaller(V1VideoPlay.class),
                                                (t) -> this.completeWithFutureStatus(play(t))
                                        )
                                )
                        )
                )
        );
    }

    private StatusCode resolveFutureToStatusCode(TypedVideoActor.GenericResponse res) {
        try {
            LOG.debug("resolveFutureToStatusCode Result={}", res);
            //TODO - not going to work
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

    private CompletionStage<StatusCode> play(V1VideoPlay request) {
        CompletionStage<TypedVideoActor.GenericResponse> ask = AskPattern.ask(
                system,
                (sender) -> new TypedVideoActor.PlayVideo(sender, request.filename, AudioOption.fromRestApi(request.audio)),
                Timeout.apply(DEFAULT_DURATION),
                system.scheduler()
        );

        return ask.thenApply(this::resolveFutureToStatusCode);
    }

    private CompletionStage<StatusCode> resume() {
        CompletionStage<TypedVideoActor.GenericResponse> ask = AskPattern.ask(
                system,
                (sender) -> new TypedVideoActor.ResumeVideo(sender),
                Timeout.apply(DEFAULT_DURATION),
                system.scheduler()
        );

        return ask.thenApply(this::resolveFutureToStatusCode);
    }

    private CompletionStage<StatusCode> pause() {
        CompletionStage<TypedVideoActor.GenericResponse> ask = AskPattern.ask(
                system,
                (sender) -> new TypedVideoActor.PauseVideo(sender),
                Timeout.apply(DEFAULT_DURATION),
                system.scheduler()
        );
        return ask.thenApply(this::resolveFutureToStatusCode);
    }

    private CompletionStage<StatusCode> stop() {
        CompletionStage<TypedVideoActor.GenericResponse> ask = AskPattern.ask(
                system,
                (sender) -> new TypedVideoActor.StopVideo(sender),
                Timeout.apply(DEFAULT_DURATION),
                system.scheduler()
        );
        return ask.thenApply(this::resolveFutureToStatusCode);
    }

    private String getFiles() {
        try {
            List<Video> files = walk(
                    ROOT_FOLDER,
                    new ArrayList<>())
                    .stream()
                    .sorted(Comparator.comparing(v -> v.name))
                    .collect(Collectors.toList());
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
