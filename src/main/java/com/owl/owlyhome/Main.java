package com.owl.owlyhome;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.owl.owlyhome.server.HttpServerNew;
import com.owl.owlyhome.video.VideoRoute;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, UnknownHostException, SocketException, InterruptedException, ExecutionException, TimeoutException {
        LOG.debug("KUPO!");
        // boot up server using the route as defined below
        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final ObjectMapper mapper = new ObjectMapper();
        VideoRoute video = new VideoRoute(system, mapper);

        HttpServerNew server = new HttpServerNew(system, materializer, video.get());
        server.start();
    }
}
