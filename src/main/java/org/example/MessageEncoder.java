package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

import java.io.IOException;

public class MessageEncoder implements Encoder.Text<ChatMessage> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String encode(ChatMessage chatMessage) throws EncodeException {
        try {
            return objectMapper.writeValueAsString(chatMessage);
        } catch (IOException e) {
            throw new EncodeException(chatMessage, "Could not encode message", e);
        }
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
    }

    @Override
    public void destroy() {
    }
}
