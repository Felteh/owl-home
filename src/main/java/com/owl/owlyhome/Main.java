package com.owl.owlyhome;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.ActorMaterializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.owl.owlyhome.light.LightRoute;
import com.owl.owlyhome.server.HttpServer;
import com.owl.owlyhome.video.TypedVideoActor;
import com.owl.owlyhome.video.VideoRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import akka.actor.typed.javadsl.Adapter;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        LOG.debug("KUPO!");
        // boot up server using the route as defined below
        final ActorSystem<TypedVideoActor.VideoCommand> system = ActorSystem.create(mainBehavior, "ChatRoomDemo");
        akka.actor.ActorSystem untypedSystem = Adapter.toUntyped(system);
        final ActorMaterializer materializer = ActorMaterializer.create(untypedSystem);

        final ObjectMapper mapper = new ObjectMapper();
        VideoRoute video = new VideoRoute(system, mapper);
        LightRoute light = new LightRoute(untypedSystem, mapper);

        HttpServer server = new HttpServer(untypedSystem, materializer, video.get(), light.get());
        server.start();
    }

    private static final Behavior<TypedVideoActor.VideoCommand> mainBehavior =
            Behaviors
                    .setup(ctx -> {
                        final ActorRef<TypedVideoActor.VideoCommand> video =
                                ctx.spawn(TypedVideoActor.BEHAVIOR, "videoActor");

                        return Behaviors.receive(TypedVideoActor.VideoCommand.class)
                                .onMessage(TypedVideoActor.VideoCommand.class, (innerCtx, msg) -> {
                                    video.tell(msg);
                                    return Behaviors.same();
                                })
                                .build();
                    });
}
