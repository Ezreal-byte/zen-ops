package com.ops.zen.utils;

import com.ops.zen.entity.LoginUser;

import java.util.Objects;

/**
 * @author xyn
 * @date 2025/4/14 20:32
 * @description
 **/
public class UserContext {

    public static final String USER_CONTEXT_KEY = "userContext";

    /**
     * 存入上下文
     * @param loginUser
     */
    public static void setUserContext(LoginUser loginUser) {
        Context.get().setAttribute(USER_CONTEXT_KEY, loginUser);
    }

    /**
     * 获取上下文中的用户信息
     * @return
     */
    public static LoginUser getUserContext() {
        LoginUser attribute = (LoginUser) Context.get().getAttribute(USER_CONTEXT_KEY);
        if (attribute == null) {
            attribute = new LoginUser();
            attribute.setPkUser(1L);
            attribute.setUserName("admin");
            attribute.setName("超级管理员");
        }
        return attribute;
    }


    public static void removeUserContext() {
        Context.get().remove(USER_CONTEXT_KEY);
    }

    public static Long getUserId() {
        LoginUser loginUser = getUserContext();
        return Objects.isNull(loginUser) ? 1L : loginUser.getPkUser();
    }

    public static String getUserName() {
        LoginUser loginUser = getUserContext();
        return Objects.isNull(loginUser) ? "admin" : loginUser.getUserName();
    }

    public static String getName() {
        LoginUser loginUser = getUserContext();
        return Objects.isNull(loginUser) ? "超级管理员" : loginUser.getName();
    }


}
