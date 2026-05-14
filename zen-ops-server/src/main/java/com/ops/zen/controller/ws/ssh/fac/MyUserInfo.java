package com.ops.zen.controller.ws.ssh.fac;

import com.jcraft.jsch.UserInfo;

/**
 * 使用私钥方式必须指定UserInfo
 *
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class MyUserInfo implements UserInfo {

    private String passphrase = null;

    public MyUserInfo(String passphrase) {
        this.passphrase = passphrase;

    }

    public String getPassphrase() {
        return passphrase;

    }

    public String getPassword() {
        return null;

    }

    public boolean promptPassphrase(String s) {
        return true;

    }

    public boolean promptPassword(String s) {
        return true;

    }

    public boolean promptYesNo(String s) {
        return true;

    }

    public void showMessage(String s) {
        System.out.println(s);
    }

}
