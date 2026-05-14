package com.ops.zen.controller.ws;

import com.jcraft.jsch.Channel;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;

/**
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class SshSession implements Closeable {

    private WebSocketSession wsSess;

    private Channel jSchChannel;

    private String sshSessionId;

    public SshSession(WebSocketSession wsSess, Channel jSchChannel, String sshSessionId) {
        this.wsSess = wsSess;
        this.jSchChannel = jSchChannel;
        this.sshSessionId = sshSessionId;
    }

    public WebSocketSession getWsSess() {
        return wsSess;
    }

    public void setWsSess(WebSocketSession wsSess) {
        this.wsSess = wsSess;
    }


    public Channel getjSchChannel() {
        return jSchChannel;
    }

    public void setjSchChannel(Channel jSchChannel) {
        this.jSchChannel = jSchChannel;
    }

    @Override
    public void close() {

        try {
            if (this.jSchChannel != null) {
                this.jSchChannel.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getSshSessionId() {
        return sshSessionId;
    }

    public void setSshSessionId(String sshSessionId) {
        this.sshSessionId = sshSessionId;
    }
}
