package com.ops.zen.controller;

import com.ops.zen.entity.ZenSysRole;
import com.ops.zen.mapper.ZenSysRoleMapper;
import com.ops.zen.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sys/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private ZenSysRoleMapper sysRoleMapper;

    @GetMapping("/list")
    public List<ZenSysRole> list() {
        return sysRoleService.list();
    }

    @PostMapping("/save")
    public String save(@RequestBody ZenSysRole role) {
        sysRoleService.save(role);
        return "ok";
    }

    @PostMapping("/update")
    public String update(@RequestBody ZenSysRole role) {
        sysRoleService.update(role);
        return "ok";
    }

    @GetMapping("/delete/{pkRole}")
    public String delete(@PathVariable Long pkRole) {
        ZenSysRole role = sysRoleMapper.selectById(pkRole);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        if (role.getIsSys() != null && role.getIsSys() == 1) {
            throw new RuntimeException("系统数据无法删除");
        }
        sysRoleService.delete(pkRole);
        return "ok";
    }

    @PostMapping("/assign-menus")
    public String assignMenus(@RequestBody Map<String, Object> params) {
        Long pkRole = Long.valueOf(params.get("pkRole").toString());
        List<?> menuIdsRaw = (List<?>) params.get("menuIds");
        List<Long> menuIds = menuIdsRaw.stream().map(o -> Long.valueOf(o.toString())).collect(java.util.stream.Collectors.toList());
        sysRoleService.assignMenus(pkRole, menuIds);
        return "ok";
    }

    @GetMapping("/menu-ids/{pkRole}")
    public List<String> menuIds(@PathVariable Long pkRole) {
        return sysRoleService.getMenuIdsByRoleId(pkRole);
    }
}
