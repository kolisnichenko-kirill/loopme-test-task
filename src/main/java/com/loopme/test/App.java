package com.loopme.test;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8080)
                .addService(new CapitalizeServiceImpl())
                .build();

        server.start();

        server.awaitTermination();
    }
}
