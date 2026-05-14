package com.ops.zen.service;

import com.ops.zen.entity.ZenSysMenu;

import java.util.List;

public interface SysMenuService {
    List<ZenSysMenu> queryMenuTree();

    List<ZenSysMenu> queryAllMenus();

    List<ZenSysMenu> queryMenusByUserId(Long pkUser);

    void saveMenu(ZenSysMenu menu);

    void updateMenu(ZenSysMenu menu);

    void deleteMenu(Long menuId);

    List<ZenSysMenu> queryMenusByRoleId(Long roleId);

    void moveMenu(Long pkMenu, String direction);
}
