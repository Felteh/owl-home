package com.owl.owlyhome;

import akka.actor.ActorSystem;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Cawcawwwwwwww?! Kupo");
        // boot up server using the route as defined below
        ActorSystem system = ActorSystem.create();

        HttpServer server = new HttpServer(system);
        server.start();
    }
}
