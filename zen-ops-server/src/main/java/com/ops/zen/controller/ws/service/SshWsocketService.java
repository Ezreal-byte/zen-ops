package com.ops.zen.controller.ws.service;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * SSH的WebSocket方式连接处理服务
 *
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public interface SshWsocketService {

    /**
     * socket连接建立
     *
     * @param session
     */
    void onConnect(WebSocketSession session);

    /**
     * socket报文到达
     *
     * @param msgStr
     * @param session
     */
    void onMessageArrival(String msgStr, WebSocketSession session);

    /**
     * 将数据传输给socket客户端
     *
     * @param session
     * @param output
     * @throws IOException
     */
    void send2WsClientBinary(WebSocketSession session, byte[] output) throws IOException;

    /**
     * socket连接关闭
     *
     * @param session
     */
    void close(WebSocketSession session);

    void sendWithoutEx(WebSocketSession session, String msg);

    void sendCmd2SshServer(WebSocketSession session, byte[] payload) throws IOException;
}
