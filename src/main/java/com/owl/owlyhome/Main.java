package com.owl.owlyhome;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.owl.owlyhome.light.LightRoute;
import com.owl.owlyhome.server.HttpServer;
import com.owl.owlyhome.video.VideoRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        LOG.debug("KUPO!");
        // boot up server using the route as defined below
        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final ObjectMapper mapper = new ObjectMapper();
        VideoRoute video = new VideoRoute(system, mapper);
        LightRoute light = new LightRoute(system, mapper);

        HttpServer server = new HttpServer(system, materializer, video.get(), light.get());
        server.start();
    }
}
