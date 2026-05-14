package com.ops.zen.controller;

import com.ops.zen.entity.ZenSysUser;
import com.ops.zen.mapper.ZenSysUserMapper;
import com.ops.zen.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xyn
 * @date 2026/4/24 13:46
 * @description
 **/
@Deprecated
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Autowired
    ZenSysUserMapper sysUserMapper2;

    @GetMapping("/hello")
    public ZenSysUser hello() {
        Long userId = UserContext.getUserId();

        ZenSysUser sysUser = sysUserMapper2.selectById(1L);
        sysUser.setBlobHeader(null);
        return sysUser;
    }
}
