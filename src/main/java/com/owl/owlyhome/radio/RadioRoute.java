package com.owl.owlyhome.radio;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.FormData;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.japi.Pair;
import akka.pattern.PatternsCS;
import akka.stream.Materializer;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class RadioRoute extends AllDirectives implements Supplier<Route> {

    private static final Logger LOG = LoggerFactory.getLogger(RadioRoute.class);

    private FiniteDuration DEFAULT_DURATION = Duration.apply(10, TimeUnit.SECONDS);
    private final ObjectMapper mapper;
    private final ActorRef radioActor;
    private final ActorSystem system;
    private final Materializer materializer;

    public RadioRoute(ActorSystem system, Materializer materializer, ObjectMapper mapper) {
        this.system = system;
        this.materializer = materializer;
        this.mapper = mapper;
        radioActor = system.actorOf(RadioActor.props(materializer), "RadioActor");
    }

    @Override
    public Route get() {
        return route(
                get(
                        () -> path(
                                PathMatchers.segment("radio").slash("stop"),
                                () -> this.completeWithFutureStatus(stop())
                        )
                ),
                post(
                        () -> route(
                                path(
                                        "radio",
                                        () -> entity(Jackson.unmarshaller(V1RadioSearch.class),
                                                (t) -> this.completeWithFuture(requestRadioStations(t))
                                        )
                                ),
                                path(
                                        PathMatchers.segment("radio").slash("play"),
                                        () -> entity(Jackson.unmarshaller(V1RadioPlay.class),
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
            if (RadioActor.SUCCESS.equals(res)) {
                return StatusCodes.OK;
            }
            if (RadioActor.FAILURE.equals(res)) {
                return StatusCodes.BAD_REQUEST;
            }
            if (RadioActor.ERROR.equals(res)) {
                return StatusCodes.INTERNAL_SERVER_ERROR;
            }
            throw new Exception("Return type of actors peculiar:" + res);
        } catch (Exception e) {
            LOG.error("Error", e);
            return StatusCodes.INTERNAL_SERVER_ERROR;
        }
    }

    private CompletionStage<StatusCode> stop() {
        CompletionStage<Object> ask = PatternsCS.ask(radioActor, RadioActor.STOP, Timeout.apply(DEFAULT_DURATION));
        return ask.thenApply(this::resolveFutureToStatusCode);
    }

    private CompletionStage<StatusCode> play(V1RadioPlay play) {
        CompletionStage<Object> ask = PatternsCS.ask(radioActor, new Play(play.station), Timeout.apply(DEFAULT_DURATION));
        return ask.thenApply(this::resolveFutureToStatusCode);
    }

    private CompletionStage<HttpResponse> requestRadioStations(V1RadioSearch search) {
        if (search.query == null || search.query.isEmpty()) {
            return getTop();
        }

        FormData formData = FormData.create(
                Pair.create("query", search.query)
        );
        return Http.get(system)
                .singleRequest(
                        HttpRequest.POST("http://uk.shoutcast.com/Search/UpdateSearch")
                                .withEntity(formData.toEntity()),
                        materializer
                );
    }

    private CompletionStage<HttpResponse> getTop() {
        return Http.get(system)
                .singleRequest(
                        HttpRequest.POST("http://uk.shoutcast.com/Home/Top"),
                        materializer
                );
    }
}
