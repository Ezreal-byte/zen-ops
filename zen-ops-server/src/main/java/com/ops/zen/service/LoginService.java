package com.ops.zen.service;

import com.ops.zen.entity.LoginUser;
import com.ops.zen.entity.request.LoginRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xyn
 * @date 2025/4/11 17:32
 * @description
 **/
public interface LoginService {
    LoginUser login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response);

    void header(Long pkUser, HttpServletResponse response);
}
