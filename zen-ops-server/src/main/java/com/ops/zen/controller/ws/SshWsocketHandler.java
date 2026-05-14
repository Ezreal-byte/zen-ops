package com.ops.zen.controller.ws;

import com.ops.zen.controller.ws.service.SshWsocketService;
import com.ops.zen.utils.ex.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.nio.ByteBuffer;


@Component
public class SshWsocketHandler implements WebSocketHandler {

    private Logger logger = LoggerFactory.getLogger(SshWsocketHandler.class);

    @Autowired
    private SshWsocketService sshWsocketService;


    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        // TODO 限制连接数量
        logger.info("初始化WebSSH连接");
        // 解决 CloseStatus[code=1009, reason=No async message support and buffer too small. Buffer size: [8,192], Message size: [2,119]]
        webSocketSession.setBinaryMessageSizeLimit(1024 * 1024); // 1MB
        //调用初始化连接
        sshWsocketService.onConnect(webSocketSession);
    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        try {
            if (webSocketMessage instanceof TextMessage) {
                // if (true) throw new RuntimeException();
                String payload = ((TextMessage) webSocketMessage).getPayload();
                logger.debug("接收到客户端命令：{}", payload);
                //调用service接收消息
                sshWsocketService.onMessageArrival(payload, webSocketSession);
            } else if (webSocketMessage instanceof BinaryMessage) {
                // now only for ZMODEM
                ByteBuffer payload = ((BinaryMessage) webSocketMessage).getPayload();
                byte[] array = payload.array();
                sshWsocketService.sendCmd2SshServer(webSocketSession, array);
            } else {
                String msg = "Unexpected WebSocket message type: " + webSocketMessage;
                sshWsocketService.sendWithoutEx(webSocketSession, msg);
                logger.error(msg);
            }
        } catch (Exception e) {
            // 不需要主动关闭会话，框架会根据异常来关闭会话
            logger.error("ssh websocket handleMessage 错误", e);
            sshWsocketService.sendWithoutEx(webSocketSession, "异常：" + Exceptions.trace(e));
            sshWsocketService.sendWithoutEx(webSocketSession, "服务端主动关闭连接");
            /*
            抛出异常会导致连接关闭，最终调用com.uis.nx.soar.common.ssh.SshWsocketHandler.afterConnectionClosed
            如下，消息处理异常会尝试关闭会话（连接）
            ExceptionWebSocketHandlerDecorator.handleMessage：
            @Override
            public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
                try {
                    getDelegate().handleMessage(session, message);
                }
                catch (Exception ex) {
                    tryCloseWithError(session, ex, logger);
                }
            }
             */
            throw e;
        }
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        logger.error("数据传输错误", throwable);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        logger.info("客户端断开或服务端主动断开");
        //调用service关闭连接
        sshWsocketService.close(webSocketSession);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
