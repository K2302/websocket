package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

import java.io.IOException;

public class MessageDecoder implements Decoder.Text<ChatMessage> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatMessage decode(String s) throws DecodeException {
        try {
            return objectMapper.readValue(s, ChatMessage.class);
        } catch (IOException e) {
            throw new DecodeException(s, "Could not decode message", e);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return (s != null);
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
    }

    @Override
    public void destroy() {
    }
}
