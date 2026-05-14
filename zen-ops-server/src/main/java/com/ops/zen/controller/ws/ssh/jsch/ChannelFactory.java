package com.ops.zen.controller.ws.ssh.jsch;

import com.ops.zen.cache.BaseMemCache;
import com.ops.zen.controller.ws.ssh.fac.*;
import com.ops.zen.utils.StringUtils;
import com.jcraft.jsch.*;
import com.ops.zen.controller.ws.ssh.fac.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 一个Session只能打开10个Channel是因为linux系统的限制
 * 统计每个Session打开的Channel数量，超过10创建新的Session：
 * 缓存Session列表，初始列表中只有一个Session，当该Session已经用尽Channel时，在创建一个Session并放入Session列表的引用中（保证缓存获取到的Session列表中有新创建的Session）
 * 同时创建并打开Channel，目前Session列表中的数量没有限制
 *
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class ChannelFactory extends BaseMemCache<SshConnCfg, List<Session>> {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(ChannelFactory.class);

    private volatile static ChannelFactory _inst;

    private ChannelFactory() {
        super(10000, 8/*8小时过期*/, TimeUnit.HOURS, (cfg, sessions) -> {// 缓存失效清理时的回调
            try {
                // guava的机制不会定时清理，失效时机不确定，主动调用invalidate时会调用该函数，断开Session（一个Session一个连接，一个线程）
                sessions.stream().forEach((Session::disconnect));
                sessions = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static ChannelFactory inst() {
        if (_inst == null) {
            synchronized (ChannelFactory.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new ChannelFactory();
            }
        }
        return _inst;
    }

    /**
     * 创建失败不要清理Session缓存，重新创建Channel，3次重试
     * 注意：Channel使用完毕自行关闭，由使用者来控制channel的生命周期
     * 缓存Session
     * 20211021 修改为直接创建Channel，不做重试
     *
     * @param sshConnCfg
     * @return
     */
    public Channel createChannel(SshConnCfg sshConnCfg) {
        DefaultSshConnCfg sshConnCfg1 = (DefaultSshConnCfg) sshConnCfg;
        String channelType = sshConnCfg1.getChannelType();
        String termType = sshConnCfg1.getTermTp();
        List<Session> sessions = get(sshConnCfg);
        // 缓存中拿到得Session集合
        for (int i = sessions.size() - 1; i >= 0; i--) { // 一个简单的优化方案，从最后一个开始取，最后一个是最新添加的Session，理论上它剩余的Channel最多
            Session s = sessions.get(i);
            try {
                return createChannelInner(s, channelType, termType);
            } catch (OverLimitException e) {
                logger.warn("Session：{}无法打开Channel，Channel数量已达限额，message：{}", s, e.getMessage());
            } catch (SessionDownException e) {
                logger.error("", e);
                // 清理会话，重新打开会话
                logger.info("会话失效 session is down，清理会话重新创建会话");
                this.invalidate(sshConnCfg);
                return createChannel(sshConnCfg);
            }
        }
        //以上Session无法创建Channel以后再次新增Session来打开Channel
        logger.warn("已经打开Session数量{}，已经打开Channel数量{}", sessions.size(), sessions.size() * 10);
        logger.warn("创建新的Session来打开Channel");
        try {
            // TODO 无限制的打开Session？
            Session session = createSessionAndConnect(sshConnCfg);
            sessions.add(session);
            return createChannelInner(session, channelType, termType);
        } catch (JSchException | OverLimitException e) {
            logger.error("", e);
            throw new RuntimeException(e);
        } catch (SessionDownException e) {
            logger.error("", e);
            // 清理会话，重新打开会话
            logger.info("会话失效 session is down，清理会话重新创建会话");
            this.invalidate(sshConnCfg);
            return createChannel(sshConnCfg);
        }


        /*try {
            return createChannelInner(sshConnCfg);
        } catch (Exception e) {
            logger.warn("当前配置无法创建Channel，" + e.getMessage());
            DefaultSshConnCfg cfg = (DefaultSshConnCfg) sshConnCfg;
            List<String> ids = cfgSessIds.get(sshConnCfg);
            // 没有关联配置
            if (ids == null || ids.size() == 0) {
                ids = new ArrayList<>();
                cfgSessIds.put(sshConnCfg, ids);
                String sessId = UUIDUtils.randomUUID();
                ids.add(sessId);
                logger.info("没有关联配置，创建关联配置获取Channel：SessionID=" + sessId);
                cfg.setSessionId(sessId);
                return createChannelInner(cfg);// 不会报Channel数量限制的错误，抛出
            }
            // 有关联配置
            if (ids != null && ids.size() > 0) {
                logger.info("有关联配置{}个，尝试已有关联配置获取Channel", ids.size());
                for (int i = 0; i < ids.size(); i++) {
                    cfg.setSessionId(ids.get(i));
                    try {
                        return createChannelInner(sshConnCfg);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        logger.warn("已有关联配置{}创建Channel报错", ids.get(i), ex);
                    }
                }
            }
            // 当现有关联配置无法获取Channel时，再增加一个新的SessionId
            String sessId = UUIDUtils.randomUUID();
            logger.warn("当现有关联配置无法获取Channel时，再增加一个新的SessionId：{}", sessId);
            try {
                ids.add(sessId);
                cfg.setSessionId(sessId);
                return createChannelInner(cfg);// 不会报Channel数量限制的错误，抛出
            } catch (Exception ex) {
                throw new RuntimeException(e);
            }

        }*/
        /*
        int i = 0;
        while (i++ < 3) {
            try {
                return createChannelInner(sshConnCfg);
            } catch (Exception e) {
                // 不要让Session失效，Session失效会
                // invalidate(sshConnCfg);
                if (i == 3) {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw new RuntimeException(e);
                }
            }
        }
        return null;// NEVER DO THIS
         */
    }

    private Channel createChannelInner(Session session, String channelType, String termType) throws SessionDownException, OverLimitException {
        try {
            if (StringUtils.isEmpty(channelType)) {
                channelType = "shell";
            } else {
                channelType = channelType.toLowerCase();
            }
            //开启shell通道
            Channel channel = session.openChannel(channelType);
            if (channel instanceof ChannelShell) {
                // 使用先进的xterm terminal type，默认为vt100（古老的终端类型，有的场景没有颜色）
                if (StringUtils.isEmpty(termType)) {
                    termType = TermTypeEn.XTERM;
                }
                ((ChannelShell) channel).setPtyType(termType);
                // 默认为false，为false时setPtySize不会生效，设置为true
                ((ChannelShell) channel).setPty(true);
            }
            //通道连接 超时时间3s
            channel.connect(3000);
            logger.info("打开Channel【{}】", channelType);
            return channel;
        } catch (Exception e) {
            if (e instanceof JSchException) {
                // 服务器重启后导致session不可用无法连接
                if ("session is down".equals(e.getMessage())) {
                    throw new SessionDownException("会话已经失效，目标服务器可能已经重启", e);
                }
                if ("channel is not opened.".equals(e.getMessage())) {
                    throw new OverLimitException("打开的Channel数量超出限制", e);
                }
            }
            throw new RuntimeException("创建Channel出错", e);
        }
    }

    @Override
    protected List<Session> load(SshConnCfg sshConnCfg) throws Exception {
        Session session = createSessionAndConnect(sshConnCfg);
        ArrayList<Session> sessions = new ArrayList<>();
        sessions.add(session);
        return sessions;
    }

    /**
     * 创建并连接SSH会话
     *
     * @param sshConnCfg
     * @return
     * @throws JSchException
     */
    private Session createSessionAndConnect(SshConnCfg sshConnCfg) throws JSchException {
        // 如果不是为每个Session都实例化一个JSch，连接多个服务器（有私钥登录，有密码登录）可能会导致无法预料验证的错误发生
        JSch jSch = new JSch();
        //获取jsch的会话
        Session session = null;
        if (sshConnCfg instanceof DefaultSshConnCfg) {
            DefaultSshConnCfg dcfg = (DefaultSshConnCfg) sshConnCfg;
            session = jSch.getSession(dcfg.getUsername(), dcfg.getHost(), dcfg.getPort());
        } else {
            throw new RuntimeException("错误的类型" + sshConnCfg.getClass());
        }
        if (sshConnCfg instanceof PasswordSshConnCfg) {
            //设置密码
            session.setPassword(((PasswordSshConnCfg) sshConnCfg).getPassword());
        } else if (sshConnCfg instanceof PrvKeySshConnCfg) {
            PrvKeySshConnCfg prvkeyCfg = (PrvKeySshConnCfg) sshConnCfg;
            byte[] prvKeyBytes = prvkeyCfg.getPrvKey().getBytes();
            jSch.addIdentity(null, prvKeyBytes, null, prvkeyCfg.getPassphrase().getBytes());
            session.setUserInfo(new MyUserInfo(null));
        }
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        //连接  超时时间30s
        session.connect(30000);
        return session;
    }

    @Override
    public boolean isInCleanAllCachePlan() {
        return false;
    }

    @Override
    public String getCacheID() {
        return "ChannelFactory";
    }


    /**
     * 超过阈值异常
     */
    static class OverLimitException extends Exception {
        public OverLimitException() {
        }

        public OverLimitException(String message) {
            super(message);
        }

        public OverLimitException(String message, Throwable cause) {
            super(message, cause);
        }

        public OverLimitException(Throwable cause) {
            super(cause);
        }

        public OverLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    /**
     * 对应异常JSchException：session is down
     * 会话失效 - 例如目标服务器重启，但Session在shell服务器上没有被清理
     */
    static class SessionDownException extends Exception {
        public SessionDownException() {
        }

        public SessionDownException(String message) {
            super(message);
        }

        public SessionDownException(String message, Throwable cause) {
            super(message, cause);
        }

        public SessionDownException(Throwable cause) {
            super(cause);
        }

        public SessionDownException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

}
