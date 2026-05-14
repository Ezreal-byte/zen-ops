package com.ops.zen.controller.ws.service.impl;

import com.ops.zen.controller.ws.SshSession;
import com.ops.zen.controller.ws.service.SshWsocketService;
import com.ops.zen.controller.ws.ssh.SshCmd;
import com.ops.zen.controller.ws.ssh.SshConst;
import com.ops.zen.controller.ws.ssh.fac.SShConnCfgFactory;
import com.ops.zen.controller.ws.ssh.fac.SshConnCfg;
import com.ops.zen.controller.ws.ssh.jsch.ChannelFactory;
import com.ops.zen.entity.LoginUser;
import com.ops.zen.entity.ZenSsh;
import com.ops.zen.service.SshServerService;
import com.ops.zen.utils.*;
import com.ops.zen.utils.ex.Exceptions;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
@Service
public class SshWsocketServiceImpl implements SshWsocketService {

    @Autowired
    SshServerService sshServerService;

    private Logger logger = LoggerFactory.getLogger(SshWsocketServiceImpl.class);

    private static Map<String, SshSession> sshSessions = new ConcurrentHashMap<>();

    private AtomicInteger ai = new AtomicInteger();

    private final int maximumPoolSize = 40;

    // TODO 限制连接数量？
    private ThreadPoolExecutor executorService = new ThreadPoolExecutor(8, maximumPoolSize, 60l, TimeUnit.SECONDS, new SynchronousQueue<>(),
            r -> new Thread(r, "t-ssh-" + ai.incrementAndGet())
            /*
            不指定RejectedExecutionHandler，默认的java.util.concurrent.ThreadPoolExecutor.AbortPolicy会在调用execute方法时抛出RejectedExecutionException异常
            , new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            System.out.println(1);
        }
    }*/
    );//Executors.newCachedThreadPool(r -> new Thread(r, "t-ssh-" +ai.incrementAndGet()));

    public void onConnect(WebSocketSession session) {
        String uuid = UUIDUtils.uuidWithoutHorizonBar();
        session.getAttributes().put(SshConst.SSH_SESSION_ID, uuid);
        sshSessions.put(uuid, new SshSession(session, null, uuid));
    }

    public String getSshSessId(WebSocketSession session) {
        return String.valueOf(session.getAttributes().get(SshConst.SSH_SESSION_ID));
    }

    public SshSession getSshSession(WebSocketSession session) {
        String sshSessId = getSshSessId(session);
        SshSession sshSession = sshSessions.get(sshSessId);
        return sshSession;
    }

    public void onMessageArrival(String msgStr, WebSocketSession session) {
        // System.out.println("in<<" + msgStr);
        SshCmd msg = JsonUtils.toObject(SshCmd.class, msgStr);
        String sshSessId = getSshSessId(session);
        SshSession sshSession = sshSessions.get(sshSessId);
        if (SshConst.MESSAGE_OP_CONNECT.equals(msg.getOp())) { // 接收连接请求命令
            String query = session.getUri().getQuery();
            Map<String, String> queryMap = HttpUtils.queryString2Map(query);
            String token = queryMap.get("token");
            LoginUser loginUser = JwtUtils.getLoginUser(token);
            //判断是否有链接权限
            SshConnCfg sshConnCfg = SShConnCfgFactory.inst().create(msg.getContent());
            ZenSsh svr = sshServerService.get(sshConnCfg.getServerId());
            if (svr == null || !svr.getPkCreatedby().equals(loginUser.getPkUser())) {
                sendWithoutEx(session, "当前用户没有此服务器的SSH权限");
                IOUtils.close(session);
                return;
            }


            Channel channel = ChannelFactory.inst().createChannel(sshConnCfg);
            //设置channel
            sshSession.setjSchChannel(channel);

            /*
            输出必须占用一个线程，因为jsch使用PipedInputStream作为读取流，该流会在read方法上进行阻塞等待，直到有数据过来
            所以无法使用NIO改造，因为无法终止等待去读取下一个Channel的输入流

            jsch的Session可以有多个Channel，Session会启动一个线程来进行服务器的通信，服务器回来的数据会在Session中分发给Channel的PipedOutputStream -> PipedInputStream.read（阻塞读取）
             */

            // begin 将ssh服务器是输出新起一个线程输出到websocket客户端
            InputStream inputStream = null;
            try {
                // 早点拿到输入流，和Session的输出建立关联
                inputStream = channel.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //启动线程异步处理
            InputStream finalInputStream = inputStream;
            try {
                executorService.execute(() -> {
                    try {
                        outputChannelStream2Client(finalInputStream, session);
                    } catch (Exception e) {
                        logger.error("webssh连接异常", e);
                        sendWithoutEx(session, "webssh连接异常，关闭会话" + Exceptions.trace(e));
                        close(session);
                    }
                    logger.warn("webssh连接结束");
                });
            } catch (RejectedExecutionException e) {
                e.printStackTrace();
                try {
                    send2WsClientBinary(session, ("输出线程池耗尽，终端总线程数量为：" + maximumPoolSize + "，技术指标【java.util.concurrent.ThreadPoolExecutor.getTaskCount】：" + executorService.getTaskCount()).getBytes());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            // end 将ssh服务器是输出新起一个线程输出到websocket客户端

            // 发送初始命令
            try {
                // 连接成功，通知客户端
                send2WsClientBinary(session, SshConst.WEBSSHSHELLCONNECTED.getBytes());
                // 切换初始路径
                String path = sshConnCfg.getInitPath();
                if (StringUtils.isEmpty(path)) {
                    path = "/home";
                }
                sendCmd2SshServer(channel, "cd " + path + "\r");
                // 执行参数带过来要执行的命令
                if (StringUtils.isNotEmpty(sshConnCfg.getInitCmd())) {
                    sendCmd2SshServer(channel, sshConnCfg.getInitCmd() + "\r");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

//            try {
//                NIOSSHConnectors.inst().bridge(channel, channel.getInputStream(),session, this);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        } else if (SshConst.MESSAGE_OP_CMD.equals(msg.getOp())) {// 接收命令
            String cmd = msg.getContent();
            try {
                sendCmd2SshServer(sshSession.getjSchChannel(), cmd);
            } catch (Exception e) {
                logger.error("webssh连接异常", e);
                sendWithoutEx(session, "webssh连接异常，关闭会话" + Exceptions.trace(e));
                close(session);
            }
        } else if (SshConst.MESSAGE_OP_RESIZE.equals(msg.getOp())) {// 接收终端大小resize命令
            Channel channel = sshSession.getjSchChannel();
            ChannelShell shell = (ChannelShell) channel;
            String content = msg.getContent();
            SshCmd.CmdResize cmdResize = JsonUtils.toObject(SshCmd.CmdResize.class, content);
            shell.setPtySize(cmdResize.getCols(), cmdResize.getRows(), 0, 0);// wp和hp给0就没问题，重要的cols和rows
        } else if (SshConst.MESSAGE_OP_HEARTBEAT.equals(msg.getOp())) {// 接收终端大小resize命令
            try {
                send2WsClientBinary(session, SshConst.HEART_BEAT_CONTENT.getBytes());
            } catch (IOException e) {

            }
        } else {
            logger.error("不支持的操作{}，关闭会话", msg.getOp());
            sendWithoutEx(session, "不支持的操作" + msg.getOp() + "，关闭会话");
            close(session);
        }
    }

    public void sendWithoutEx(WebSocketSession session, String msg) {
        try {
            send2WsClientBinary(session, msg.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Override
    public void send2WsClientBinary(WebSocketSession session, byte[] output) throws IOException {
        // 客户端指定了按二进制传输数据以后，TextMessage无法接收到到
        // session.sendMessage(new TextMessage(output));
        session.sendMessage(new BinaryMessage(output));
    }

    @Override
    public void close(WebSocketSession session) {
        String sshSessId = getSshSessId(session);
        SshSession sshSession = sshSessions.get(sshSessId);
        if (sshSession != null) {
            //断开连接
            sshSession.close();
            //map中移除
            sshSessions.remove(sshSessId);
        }
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void outputChannelStream2Client(InputStream inputStream, WebSocketSession wsSession) throws Exception {
        //读取ssh server返回的信息流
        // InputStream inputStream = channel.getInputStream();
        try {
            //循环读取
            byte[] buffer = new byte[4096];
            int i = 0;
            //如果没有数据来，线程会一直阻塞在这个地方等待数据。
            while ((i = inputStream.read(buffer)) != -1) {
                byte[] output = Arrays.copyOfRange(buffer, 0, i);
                // System.out.println("out>>" + StringUtils.bytes2OriginString(output));
                // System.out.println("out>>" + new String(output, "UTF-8"));
                send2WsClientBinary(wsSession, output);
            }
            // 当websocket关闭时，会调用channel的disconnect方法，这时inputStream.read不会抛出异常，而是会返回-1
            if (i == -1) {
                logger.warn("shell channel被关闭");
            }
        } catch (Exception ex) {
            logger.warn("和客户端通信异常，将会断开连接");
            throw ex;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            sendWithoutEx(wsSession, "读取ssh server返回的信息流异常结束");
        }
    }

    private void sendCmd2SshServer(Channel channel, String command) throws IOException {
        if (channel != null) {
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();
        }
    }

    @Override
    public void sendCmd2SshServer(WebSocketSession session, byte[] payload) throws IOException {
        Channel channel = getSshSession(session).getjSchChannel();
        OutputStream outputStream = channel.getOutputStream();
        outputStream.write(payload);
        outputStream.flush();
    }
}
