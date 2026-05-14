package com.ops.zen.controller.ws;

import com.ops.zen.utils.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author xyn
 * @Date 2025/04/12
 * @Description
 */
@Component
public class HandshakeInterceptor implements org.springframework.web.socket.server.HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        HttpServletRequest httpRequest = ((ServletServerHttpRequest) request).getServletRequest();
        HttpServletResponse httpResponse = ((ServletServerHttpResponse) response).getServletResponse();

        /*
        子协议：Sec-WebSocket-Protocol需要在握手阶段响应
        A client has to ask for a specific subprotocol. To do so, it will send something like this as part of the original handshake:
        https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_servers
         */
        String key = "Sec-WebSocket-Protocol";
        if (StringUtils.isNotEmpty(httpRequest.getHeader(key))) {
            httpResponse.addHeader(key, httpRequest.getHeader(key));
        }
    }
}
