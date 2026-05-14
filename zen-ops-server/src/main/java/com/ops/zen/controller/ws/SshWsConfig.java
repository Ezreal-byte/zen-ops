package com.ops.zen.controller.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author xyn
 * @date 2026/4/27
 * @description SSH WebSocket配置（新端点，与原有的/commons/ssh并行）
 */
@Configuration
@EnableWebSocket
public class SshWsConfig implements WebSocketConfigurer {

    @Autowired
    private SshWsHandler sshWsHandler;

    @Autowired
    private HandshakeInterceptor handshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sshWsHandler, "/ws/ssh")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
