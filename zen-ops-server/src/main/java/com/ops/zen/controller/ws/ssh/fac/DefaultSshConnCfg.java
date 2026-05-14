package com.ops.zen.controller.ws.ssh.fac;

import java.util.Objects;

/**
 * 注意，该类会被作为ChannelFactory缓存的key来获取{@link com.jcraft.jsch.Session}，所以参与key的组成的字段为：username，host，port，实现类的{@link PasswordSshConnCfg#getPassword()}
 * {@link PrvKeySshConnCfg#getPrvKey()}，{@link PrvKeySshConnCfg#getPassphrase()} 这些产于Session创建的必要条件
 * equals和hashCode方法只由这些字段参与运算
 * 目的是用来减少Session的实例（一个Session对应一个线程）
 *
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class DefaultSshConnCfg implements SshConnCfg {

    protected String host;

    protected int port;

    protected String username;

    protected String type;

    protected String channelType;

    protected String initPath;

    /**
     * 描述
     */
    protected String des;

    /**
     * type == ID时可用
     */
    protected String id;

    protected String idType;

    /**
     * 初始命令
     */
    protected String initCmd;

    protected String termTp;

    protected Long serverId;

    public DefaultSshConnCfg() {
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getInitPath() {
        return initPath;
    }

    public void setInitPath(String initPath) {
        this.initPath = initPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    @Override
    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getInitCmd() {
        return initCmd;
    }

    public void setInitCmd(String initCmd) {
        this.initCmd = initCmd;
    }

    @Override
    public String getTermTp() {
        return termTp;
    }

    public void setTermTp(String termTp) {
        this.termTp = termTp;
    }

    @Override
    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultSshConnCfg)) return false;
        DefaultSshConnCfg that = (DefaultSshConnCfg) o;
        return getPort() == that.getPort() &&
                Objects.equals(getHost(), that.getHost()) &&
                Objects.equals(getUsername(), that.getUsername()); // &&
//                Objects.equals(getType(), that.getType()) &&
//                Objects.equals(getChannelType(), that.getChannelType()) &&
//                Objects.equals(getInitPath(), that.getInitPath()) &&
//                Objects.equals(getDes(), that.getDes()) &&
//                Objects.equals(getIdType(), that.getIdType()) &&
//                Objects.equals(getInitCmd(), that.getInitCmd()) &&
//                Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
//        return Objects.hash(getHost(), getPort(), getUsername(), getType(), getChannelType(), getInitPath(), getDes(), getId(), getIdType(), getInitCmd());
        return Objects.hash(getHost(), getPort(), getUsername());
    }
}
