package org.example;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@ServerEndpoint(value = "/chat/{room}", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class ChatEndpoint {

    private static final Map<String, Set<Session>> roomSessions = new ConcurrentHashMap<>();
    private static final Map<String, List<ChatMessage>> roomHistory = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY = 5;

    @OnOpen
    public void onOpen(@PathParam("room") String room, Session session) {
        // Break down: roomSessions.computeIfAbsent(...)
        Set<Session> sessions = roomSessions.get(room);
        if (sessions == null) {
            synchronized (roomSessions) {
                sessions = roomSessions.get(room); // Check again inside lock
                if (sessions == null) {
                    sessions = Collections.synchronizedSet(new HashSet<>());
                    roomSessions.put(room, sessions);
                }
            }
        }
        sessions.add(session);

        System.out.println("New connection to room [" + room + "]: " + session.getId());

        // Send history to new user
        List<ChatMessage> history = roomHistory.get(room);
        if (history != null) {
            synchronized (history) {
                for (ChatMessage msg : history) {
                    try {
                        session.getBasicRemote().sendObject(msg);
                    } catch (IOException | EncodeException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @OnMessage
    public void onMessage(@PathParam("room") String room, ChatMessage message, Session session)
            throws IOException, EncodeException {
        System.out.println("Room [" + room + "] message from " + message.getSender() + ": " + message.getContent());

        // Break down: roomHistory.computeIfAbsent(...)
        List<ChatMessage> history = roomHistory.get(room);
        if (history == null) {
            synchronized (roomHistory) {
                history = roomHistory.get(room); // Check again inside lock
                if (history == null) {
                    history = Collections.synchronizedList(new LinkedList<>());
                    roomHistory.put(room, history);
                }
            }
        }

        synchronized (history) {
            if (history.size() >= MAX_HISTORY) {
                history.remove(0);
            }
            history.add(message);
        }

        broadcast(room, message);
    }

    @OnClose
    public void onClose(@PathParam("room") String room, Session session) {
        Set<Session> sessions = roomSessions.get(room);
        if (sessions != null) {
            sessions.remove(session);
        }
        System.out.println("Connection closed in room [" + room + "]: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error on session " + session.getId() + ": " + throwable.getMessage());
    }

    private void broadcast(String room, ChatMessage message) throws IOException, EncodeException {
        Set<Session> sessions = roomSessions.get(room);
        if (sessions != null) {
            synchronized (sessions) {
                for (Session session : sessions) {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendObject(message);
                    }
                }
            }
        }
    }
}
