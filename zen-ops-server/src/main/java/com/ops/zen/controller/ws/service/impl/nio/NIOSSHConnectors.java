package com.ops.zen.controller.ws.service.impl.nio;

import com.ops.zen.controller.ws.service.SshWsocketService;
import com.ops.zen.utils.ThreadUtils;
import com.jcraft.jsch.Channel;
import org.slf4j.Logger;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * NIO的方式处理ssh服务器到Websocket端的报文传输
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class NIOSSHConnectors {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(NIOSSHConnectors.class);

    private volatile static NIOSSHConnectors _inst;

    private NIOSSHConnectors() {


        new Thread(() -> {
            while (true) {
                if (conns.size() == 0) {
                    ThreadUtils.sleepWithoutEx(100);
                    continue;
                }
//                ConnectObj c = conns.get(0);
                conns.forEach((c -> {
                    //读取ssh server返回的信息流
                    InputStream inputStream = c.inputStream;
                    try {
//                        inputStream = c.channel.getInputStream();
                        //循环读取
                        byte[] buffer = new byte[4096];
                        int i = 0;
                        if ((i = inputStream.read(buffer)) != -1) {
                            byte[] output = Arrays.copyOfRange(buffer, 0, i);
                            // System.out.println("out>>" + StringUtils.bytes2OriginString(output));
                            // System.out.println("out>>" + new String(output, "UTF-8"));
                            c.service.send2WsClientBinary(c.session, output);
                        }
                        // 当websocket关闭时，会调用channel的disconnect方法，这时inputStream.read不会抛出异常，而是会返回-1
                        if (i == -1) {
                            logger.warn("shell channel被关闭");
                        }
                    } catch (Exception ex) {
                        logger.warn("和客户端通信异常，将会断开连接");
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        c.service.sendWithoutEx(c.session, "读取ssh server返回的信息流异常结束");
                        c.service.close(c.session);
                    } finally {
                    }

                }));
            }
        }, "nio-ssh-connectors").start();
    }

    public static NIOSSHConnectors inst() {
        if (_inst == null) {
            synchronized (NIOSSHConnectors.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new NIOSSHConnectors();
            }
        }
        return _inst;
    }

    List<ConnectObj> conns = new ArrayList<>();

    public void bridge(Channel channel, InputStream inputStream, WebSocketSession session, SshWsocketService service) {
        ConnectObj key = new ConnectObj(channel, inputStream, session, service);
        conns.add(key);
    }

    static class ConnectObj {
        Channel channel;

        InputStream inputStream;

        WebSocketSession session;

        SshWsocketService service;

        public ConnectObj(Channel channel, InputStream inputStream, WebSocketSession session, SshWsocketService service) {
            this.channel = channel;
            this.inputStream = inputStream;
            this.session = session;
            this.service = service;
        }
    }
}
