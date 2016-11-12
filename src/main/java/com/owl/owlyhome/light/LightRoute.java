package com.owl.owlyhome.light;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class LightRoute extends AllDirectives implements Supplier<Route> {

    private static final Logger LOG = LoggerFactory.getLogger(LightRoute.class);

    private FiniteDuration DEFAULT_DURATION = Duration.apply(10, TimeUnit.SECONDS);
    private final ObjectMapper mapper;
    private final Map<LightGroup, ActorRef> lightActors = new HashMap<>();

    public LightRoute(ActorSystem system, ObjectMapper mapper) {
        this.mapper = mapper;
        lightActors.put(LightGroup.LoftLounge, system.actorOf(LightActor.props(LightGroup.LoftLounge), "LoftLoungeActor"));
    }

    @Override
    public Route get() {
        return route(
                get(
                        () -> route(
                                path(
                                        "lights",
                                        () -> this.complete(getLights())
                                )
                        )
                ),
                post(
                        () -> route(
                                path(
                                        "lights",
                                        () -> entity(
                                                Jackson.unmarshaller(V1LightRequest.class),
                                                (t) -> this.completeWithFutureStatus(lightRequest(t))
                                        )
                                )
                        )
                )
        );
    }

    private String getLights() {
        try {
            List<Light> files = Arrays.asList(new Light(LightGroup.LoftLounge));
            return mapper.writeValueAsString(files);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private StatusCode resolveFutureToStatusCode(Object res) {
        try {
            LOG.debug("resolveFutureToStatusCode Result={}", res);
            if (LightActor.SUCCESS.equals(res)) {
                return StatusCodes.OK;
            }
            throw new Exception("Return type of actors peculiar:" + res);
        } catch (Exception e) {
            LOG.error("Error", e);
            return StatusCodes.INTERNAL_SERVER_ERROR;
        }
    }

    private StatusCode resolveFailureToStatusCode(Throwable res) {
        LOG.error("resolveFailureToStatusCode Result={}", res);
        return StatusCodes.INTERNAL_SERVER_ERROR;
    }

    private CompletionStage<StatusCode> lightRequest(V1LightRequest t) {
        ActorRef actor = lightActors.get(LightGroup.fromMacAddress(t.lightAddress));
        String command;
        if (t.state.equals("on")) {
            command = LightActor.ON;
        } else {
            command = LightActor.OFF;
        }
        CompletionStage<Object> ask = PatternsCS.ask(actor, command, Timeout.apply(DEFAULT_DURATION));
        return ask.thenApply(this::resolveFutureToStatusCode).exceptionally(this::resolveFailureToStatusCode);
    }
}
