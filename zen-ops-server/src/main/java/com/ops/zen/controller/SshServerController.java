package com.ops.zen.controller;

import com.ops.zen.entity.LoginUser;
import com.ops.zen.entity.ZenSsh;
import com.ops.zen.entity.response.ZenSshListResponse;
import com.ops.zen.service.SshServerService;
import com.ops.zen.utils.UserContext;
import com.ops.zen.utils.map.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xyn
 * @date 2025/4/11 15:01
 * @description
 **/
//@RestController
//@RequestMapping("/ssh/server")
//@Slf4j
@Deprecated
public class SshServerController {

    @Autowired
    private SshServerService service;

    @PostMapping("add")
    public String add(@RequestBody ZenSsh sshServer, HttpServletRequest request) {
        LoginUser loginUser = UserContext.getUserContext();
        return service.add(sshServer, loginUser);
    }

    @GetMapping("delete")
    public String delete(@RequestParam Long pkServer, HttpServletRequest request) {
        LoginUser loginUser = UserContext.getUserContext();
        return service.delete(pkServer, loginUser);
    }

    @PostMapping("update")
    public String update(@RequestBody ZenSsh sshServer, HttpServletRequest request) {
        LoginUser loginUser = UserContext.getUserContext();
        return service.update(sshServer, loginUser);
    }

    @PostMapping("list")
    public PageResult<ZenSshListResponse> list(@RequestBody ZenSsh sshServer, Integer pageNum, Integer pageSize,
                                               HttpServletRequest request) {
        LoginUser loginUser = UserContext.getUserContext();
        return service.list(sshServer, pageNum, pageSize, loginUser);
    }

    @GetMapping("get")
    public ZenSshListResponse get(@RequestParam Long pkServer, HttpServletRequest request) {
        LoginUser loginUser = UserContext.getUserContext();
        return service.get(pkServer, loginUser);
    }
}
