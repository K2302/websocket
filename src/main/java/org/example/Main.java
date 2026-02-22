package org.example;

import org.glassfish.tyrus.server.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        Server server = new Server("localhost", 8025, "/ws", null, ChatEndpoint.class);

        try {
            server.start();
            System.out.println("--- WebSocket Server started at ws://localhost:8025/ws/chat ---");
            System.out.println("Press Enter to stop the server...");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }
}