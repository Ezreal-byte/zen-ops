package com.ops.zen.controller.ws.ssh.fac;

import java.util.Objects;

/**
 * 私钥密码方式的配置
 *
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class PrvKeySshConnCfg extends DefaultSshConnCfg {

    /**
     * 私钥
     */
    private String prvKey;

    /**
     * 私钥密码
     */
    private String passphrase;

    public PrvKeySshConnCfg() {
    }

    public PrvKeySshConnCfg(String channelType, String host, int port, String username, String prvKey, String passphrase, String des) {
        this.channelType = channelType;
        this.host = host;
        this.port = port;
        this.username = username;
        this.prvKey = prvKey;
        this.passphrase = passphrase;
        this.des = des;

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

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getPrvKey() {
        return prvKey;
    }

    public void setPrvKey(String prvKey) {
        this.prvKey = prvKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrvKeySshConnCfg)) return false;
        if (!super.equals(o)) return false;
        PrvKeySshConnCfg that = (PrvKeySshConnCfg) o;
        return Objects.equals(getPrvKey(), that.getPrvKey()) &&
                Objects.equals(getPassphrase(), that.getPassphrase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPrvKey(), getPassphrase());
    }
}
