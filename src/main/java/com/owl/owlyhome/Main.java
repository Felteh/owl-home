package com.owl.owlyhome;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.owl.owlyhome.server.HttpServerNew;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Main {

    public static void main(String[] args) throws IOException, UnknownHostException, SocketException, InterruptedException, ExecutionException, TimeoutException {
        System.out.println("Kupo");
        // boot up server using the route as defined below
        ActorSystem system = ActorSystem.create();
        ActorMaterializer materializer = ActorMaterializer.create(system);
        HttpServerNew server = new HttpServerNew(system, materializer);
        server.start();
    }
}
