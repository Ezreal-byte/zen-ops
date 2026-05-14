package com.ops.zen.controller;

import com.ops.zen.entity.LoginUser;
import com.ops.zen.entity.ZenSsh;
import com.ops.zen.entity.response.ZenSshListResponse;
import com.ops.zen.service.SshService;
import com.ops.zen.ssh.vo.ServerInfoVo;
import com.ops.zen.utils.UserContext;
import com.ops.zen.utils.en.EnUtils;
import com.ops.zen.en.SshLoginTypeEn;
import com.ops.zen.utils.map.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * SSH数据源管理
 * @Date 2026/4/27
 */
@RestController
@RequestMapping("/ssh/ds")
@Slf4j
public class SshDsController {

    @Autowired
    private SshService sshService;

    @PostMapping("add")
    public String add(@RequestBody ZenSsh sshServer) {
        sshService.add(sshServer);
        return null;
    }

    @GetMapping("delete/{pkServer}")
    public String delete(@PathVariable Long pkServer) {
        sshService.delete(pkServer);
        return null;
    }

    @PostMapping("update")
    public String update(@RequestBody ZenSsh sshServer) {
        sshService.update(sshServer);
        return null;
    }

    @GetMapping("get/{pkServer}")
    public ZenSsh get(@PathVariable Long pkServer) {
        return sshService.get(pkServer);
    }

    @PostMapping("list")
    public PageResult<ZenSshListResponse> list(@RequestBody ZenSsh sshServer, Integer pageNum, Integer pageSize,
                                               HttpServletRequest request) {
        LoginUser loginUser = UserContext.getUserContext();
        return sshService.list(sshServer, pageNum, pageSize, loginUser);
    }

    @GetMapping("loginTypes")
    public List<Map<String, Object>> loginTypes() {
        return EnUtils.toSelectModels(SshLoginTypeEn.class)
                .stream().map(sm -> {
                    java.util.LinkedHashMap<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("code", sm.getValue());
                    m.put("label", sm.getLabel());
                    return m;
                }).collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("test/{pkServer}")
    public Map<String, Object> testConnection(@PathVariable Long pkServer) {
        return sshService.testConnection(pkServer);
    }

    @GetMapping("info/{pkServer}")
    public ServerInfoVo getServerInfo(@PathVariable Long pkServer) {
        return sshService.getServerInfo(pkServer);
    }

    /**
     * 获取服务器资源使用情况（CPU、内存、磁盘占用率）
     */
    @GetMapping("usage/{pkServer}")
    public ServerInfoVo.UsageVo getServerUsage(@PathVariable Long pkServer) {
        return sshService.getServerUsage(pkServer);
    }

    /**
     * 收藏/取消收藏服务器
     */
    @PostMapping("favorite/{pkServer}")
    public String toggleFavorite(@PathVariable Long pkServer) {
        sshService.toggleFavorite(pkServer);
        return null;
    }

    /**
     * 更新服务器标签
     */
    @PostMapping("tags/{pkServer}")
    public String updateTags(@PathVariable Long pkServer, @RequestParam String tags) {
        sshService.updateTags(pkServer, tags);
        return null;
    }
}
