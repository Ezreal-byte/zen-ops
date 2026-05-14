package com.ops.zen.controller.ws;

import com.ops.zen.controller.ws.ssh.SshConst;
import com.ops.zen.controller.ws.ssh.fac.SShConnCfgFactory;
import com.ops.zen.controller.ws.ssh.fac.SshConnCfg;
import com.ops.zen.controller.ws.ssh.jsch.ChannelFactory;
import com.ops.zen.entity.ZenSsh;
import com.ops.zen.mapper.ZenSshMapper;
import com.ops.zen.utils.JsonUtils;
import com.ops.zen.utils.StringUtils;
import com.ops.zen.utils.ex.Exceptions;
import com.jcraft.jsch.Channel;
import com.ops.zen.controller.ws.ssh.SshCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author xyn
 * @date 2026/4/27
 * @description SSH WebSocket处理器（简化版，直接用SshServer ID连接）
 */
@Component
public class SshWsHandler extends BinaryWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(SshWsHandler.class);

    @Autowired
    private ZenSshMapper sshServerMapper;

    private static final Map<String, SshSession> sessions = new ConcurrentHashMap<>();
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            8, 40, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
            r -> new Thread(r, "t-ssh-ds-" + System.nanoTime())
    );

    @Override
    public void afterConnectionEstablished(WebSocketSession wsSession) throws Exception {
        wsSession.setBinaryMessageSizeLimit(1024 * 1024);
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        wsSession.getAttributes().put(SshConst.SSH_SESSION_ID, uuid);
        sessions.put(uuid, new SshSession(wsSession, null, uuid));
        logger.info("SSH WebSocket连接建立: {}", uuid);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession wsSession, BinaryMessage message) throws Exception {
        byte[] payload = message.getPayload().array();
        SshSession sshSession = getSshSession(wsSession);
        if (sshSession != null && sshSession.getjSchChannel() != null) {
            OutputStream os = sshSession.getjSchChannel().getOutputStream();
            os.write(payload);
            os.flush();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession wsSession, TextMessage message) {
        try {
            String payload = message.getPayload();
            SshCmd cmd = JsonUtils.toObject(SshCmd.class, payload);
            String op = cmd.getOp();

            if (SshConst.MESSAGE_OP_CONNECT.equals(op)) {
                handleConnect(cmd.getContent(), wsSession);
            } else if (SshConst.MESSAGE_OP_CMD.equals(op)) {
                handleCmd(cmd.getContent(), wsSession);
            } else if (SshConst.MESSAGE_OP_RESIZE.equals(op)) {
                handleResize(cmd.getContent(), wsSession);
            } else if (SshConst.MESSAGE_OP_HEARTBEAT.equals(op)) {
                sendWithoutEx(wsSession, SshConst.HEART_BEAT_CONTENT);
            }
        } catch (Exception e) {
            logger.error("SSH WebSocket消息处理异常", e);
            sendWithoutEx(wsSession, "异常：" + Exceptions.trace(e));
            throw new RuntimeException(e);
        }
    }

    private void handleConnect(String content, WebSocketSession wsSession) throws Exception {
        SshSession sshSession = getSshSession(wsSession);
        // content是SshConnCfg JSON，使用SShConnCfgFactory创建
        SshConnCfg sshConnCfg = SShConnCfgFactory.inst().create(content);
        Channel channel = ChannelFactory.inst().createChannel(sshConnCfg);
        sshSession.setjSchChannel(channel);

        InputStream inputStream = channel.getInputStream();
        executor.execute(() -> {
            try {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byte[] output = Arrays.copyOfRange(buffer, 0, len);
                    wsSession.sendMessage(new BinaryMessage(output));
                }
            } catch (Exception e) {
                logger.warn("SSH输出线程结束", e);
            }
        });

        // 连接成功通知
        sendWithoutEx(wsSession, SshConst.WEBSSHSHELLCONNECTED);

        // 切换到默认目录
        String initPath = sshConnCfg.getInitPath();
        if (StringUtils.isNotEmpty(initPath)) {
            sendCmd2Channel(channel, "cd " + initPath + "\r");
        }
    }

    private void handleCmd(String cmd, WebSocketSession wsSession) throws Exception {
        SshSession sshSession = getSshSession(wsSession);
        if (sshSession != null && sshSession.getjSchChannel() != null) {
            sendCmd2Channel(sshSession.getjSchChannel(), cmd);
        }
    }

    private void handleResize(String content, WebSocketSession wsSession) throws Exception {
        SshSession sshSession = getSshSession(wsSession);
        if (sshSession != null && sshSession.getjSchChannel() != null) {
            SshCmd.CmdResize resize =
                    JsonUtils.toObject(SshCmd.CmdResize.class, content);
            com.jcraft.jsch.ChannelShell shell = (com.jcraft.jsch.ChannelShell) sshSession.getjSchChannel();
            shell.setPtySize(resize.getCols(), resize.getRows(), 0, 0);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession wsSession, CloseStatus status) throws Exception {
        logger.info("SSH WebSocket连接关闭");
        closeSession(wsSession);
    }

    @Override
    public void handleTransportError(WebSocketSession wsSession, Throwable throwable) throws Exception {
        logger.error("SSH WebSocket传输错误", throwable);
        closeSession(wsSession);
    }

    private void closeSession(WebSocketSession wsSession) {
        SshSession sshSession = getSshSession(wsSession);
        if (sshSession != null) {
            sshSession.close();
            sessions.remove(sshSession.getSshSessionId());
        }
        try { wsSession.close(); } catch (Exception ignored) {}
    }

    private void sendCmd2Channel(Channel channel, String cmd) throws Exception {
        if (channel != null) {
            OutputStream os = channel.getOutputStream();
            os.write(cmd.getBytes());
            os.flush();
        }
    }

    private void sendWithoutEx(WebSocketSession session, String msg) {
        try {
            session.sendMessage(new BinaryMessage(msg.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    private ZenSsh getSshServer(Long pkServer) {
        return sshServerMapper.selectById(pkServer);
    }

    private SshSession getSshSession(WebSocketSession wsSession) {
        String id = String.valueOf(wsSession.getAttributes().get(SshConst.SSH_SESSION_ID));
        return sessions.get(id);
    }
}
