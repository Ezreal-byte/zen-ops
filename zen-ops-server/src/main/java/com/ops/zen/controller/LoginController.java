package com.ops.zen.controller;

import com.ops.zen.entity.LoginUser;
import com.ops.zen.entity.request.LoginRequest;
import com.ops.zen.service.LoginService;
import com.ops.zen.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xyn
 * @date 2025/4/11 17:29
 * @description
 **/
//@RestController
//@RequestMapping("/auth")
@Slf4j
public class LoginController {

    @Autowired
    LoginService loginService;


    /**
     * 登录
     * @param loginRequest
     * @param request
     * @param response
     * @return
     */
    @PostMapping("login")
    public LoginUser login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        return loginService.login(loginRequest, request, response);
    }

    /**
     * 登出
     * @param request
     * @param response
     * @return
     */
    @PostMapping("logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
//        return loginService.logout(request, response);
        return "ok";
    }

    /**
     * 获取用户头像
     * @param request
     * @param response
     */
    @GetMapping("header")
    public void header(HttpServletRequest request, HttpServletResponse response) {
        LoginUser loginUser = JwtUtils.getLoginUser(request);
        loginService.header(loginUser.getPkUser(), response);
    }
}
