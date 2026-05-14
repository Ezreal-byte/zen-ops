package com.ops.zen.controller;

import com.ops.zen.entity.ZenSysMenu;
import com.ops.zen.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sys/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @GetMapping("/tree")
    public List<ZenSysMenu> tree() {
        return sysMenuService.queryMenuTree();
    }

    @GetMapping("/list")
    public List<ZenSysMenu> list() {
        return sysMenuService.queryAllMenus();
    }

    @PostMapping("/save")
    public String save(@RequestBody ZenSysMenu menu) {
        sysMenuService.saveMenu(menu);
        return "ok";
    }

    @PostMapping("/update")
    public String update(@RequestBody ZenSysMenu menu) {
        sysMenuService.updateMenu(menu);
        return "ok";
    }

    @GetMapping("/delete/{menuId}")
    public String delete(@PathVariable Long menuId) {
        sysMenuService.deleteMenu(menuId);
        return "ok";
    }

    @GetMapping("/role-menus/{roleId}")
    public List<ZenSysMenu> roleMenus(@PathVariable Long roleId) {
        return sysMenuService.queryMenusByRoleId(roleId);
    }

    @PostMapping("/move")
    public String move(@RequestBody Map<String, Object> params) {
        Long pkMenu = Long.valueOf(params.get("pkMenu").toString());
        String direction = params.get("direction").toString();
        sysMenuService.moveMenu(pkMenu, direction);
        return "ok";
    }
}
