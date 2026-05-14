package com.ops.zen.controller.ws.ssh.fac;

import java.util.Objects;

/**
 * 用户名密码的方式的配置
 *
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class PasswordSshConnCfg extends DefaultSshConnCfg {


    private String password;

    public PasswordSshConnCfg() {
    }

    public PasswordSshConnCfg(String channelType, String host, int port, String username, String password,String des) {
        this.channelType = channelType;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PasswordSshConnCfg)) return false;
        if (!super.equals(o)) return false;
        PasswordSshConnCfg that = (PasswordSshConnCfg) o;
        return Objects.equals(getPassword(), that.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPassword());
    }
}
