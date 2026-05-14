package com.ops.zen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ops.zen.entity.LoginUser;
import com.ops.zen.entity.ZenSysMenu;
import com.ops.zen.entity.ZenSysRole;
import com.ops.zen.entity.ZenSysRoleUser;
import com.ops.zen.entity.request.LoginRequest;
import com.ops.zen.mapper.ZenSysRoleMapper;
import com.ops.zen.mapper.ZenSysRoleUserMapper;
import com.ops.zen.service.LoginService;
import com.ops.zen.service.SysMenuService;
import com.ops.zen.utils.JwtUtils;
import com.ops.zen.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Slf4j
public class LoginController2 {

    @Autowired
    LoginService loginService;

    @Autowired
    SysMenuService sysMenuService;

    @Autowired
    ZenSysRoleUserMapper sysRoleUserMapper;

    @Autowired
    ZenSysRoleMapper sysRoleMapper;

    @GetMapping("/getUserInfo")
    public Map<String, Object> getUserInfo(HttpServletRequest request) {
        LoginUser user = UserContext.getUserContext();
        if (user == null) {
            user = JwtUtils.getLoginUser(request);
        }
        // 查询用户角色中文名
        List<String> roleNames = getRoleNamesByUserId(user.getPkUser());
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getPkUser());
        map.put("userName", user.getUserName());
        map.put("name", user.getName());
        map.put("roles", roleNames);
        return map;
    }

    private List<String> getRoleNamesByUserId(Long userId) {
        QueryWrapper<ZenSysRoleUser> ruWrapper = new QueryWrapper<>();
        ruWrapper.eq("pk_user", userId);
        List<ZenSysRoleUser> roleUsers = sysRoleUserMapper.selectList(ruWrapper);
        if (roleUsers == null || roleUsers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = roleUsers.stream()
                .map(ZenSysRoleUser::getPkRole)
                .collect(Collectors.toList());
        QueryWrapper<ZenSysRole> roleWrapper = new QueryWrapper<>();
        roleWrapper.in("pk_role", roleIds);
        List<ZenSysRole> roles = sysRoleMapper.selectList(roleWrapper);
        return roles.stream().map(ZenSysRole::getName).collect(Collectors.toList());
    }

    @PostMapping("login")
    public Map<String, String> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        LoginUser login = loginService.login(loginRequest, request, response);
        String token = JwtUtils.getToken(login);
        response.setHeader(JwtUtils.JWT_RESPONSE_HEADER_TOKEN, token);
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", token);
        return result;
    }

    @PostMapping("logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        return "ok";
    }

    @GetMapping("header")
    public void header(HttpServletRequest request, HttpServletResponse response) {
        LoginUser loginUser = JwtUtils.getLoginUser(request);
        if (loginUser == null) {
            return;
        }
        loginService.header(loginUser.getPkUser(), response);
    }

    @GetMapping("/menus")
    public List<ZenSysMenu> menus() {
        LoginUser user = UserContext.getUserContext();
        if (user == null) {
            return Collections.emptyList();
        }
        return sysMenuService.queryMenusByUserId(user.getPkUser());
    }
}
