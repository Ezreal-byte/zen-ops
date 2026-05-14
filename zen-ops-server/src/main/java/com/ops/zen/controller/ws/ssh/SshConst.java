package com.ops.zen.controller.ws.ssh;

/**
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class SshConst {

    /**
     * SSH会话集合【MAP】使用的KEY
     */
    public static final String SSH_SESSION_ID = "SSH_SESSION_ID";

    /**
     * ssh操作类型-连接
     */
    public static final String MESSAGE_OP_CONNECT = "connect";

    /**
     * ssh操作类型-命令（例：ls，pwd，cd等）
     */
    public static final String MESSAGE_OP_CMD = "cmd";

    /**
     * ssh终端大小重新适配命令
     */
    public static final String MESSAGE_OP_RESIZE = "resize";

    /**
     * 心跳命令
     */
    public static final String MESSAGE_OP_HEARTBEAT = "heartbeat";

    /**
     * 服务器发送给客户端的心跳传输的报文
     */
    public static final String HEART_BEAT_CONTENT = "________heart-resp________";

    /**
     * shell连接成功以后，后台主动发送给客户端的消息内容，客户端可以在连接成功以后发送一些初始命令，例如MESSAGE_OP_RESIZE
     */
    public static final String WEBSSHSHELLCONNECTED = "websshshellconnected";
}
