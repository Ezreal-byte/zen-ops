package com.ops.zen.controller.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class SshWsocketConfig implements WebSocketConfigurer {

    @Autowired
    SshWsocketHandler SSHWsocketHandler;

    @Autowired
    HandshakeInterceptor handshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        //socket通道
        //指定处理器和路径
        webSocketHandlerRegistry.addHandler(SSHWsocketHandler, "/commons/ssh")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
