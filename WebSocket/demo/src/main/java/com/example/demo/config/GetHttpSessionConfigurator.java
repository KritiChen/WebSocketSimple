package com.example.demo.config;

import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
@Configuration
public class GetHttpSessionConfigurator extends ServerEndpointConfig.Configurator{
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        HttpSession hs = (HttpSession) request.getHttpSession();
        sec.getUserProperties().put(HttpSession.class.getName(), hs);
    }
}
