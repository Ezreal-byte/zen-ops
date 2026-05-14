package com.ops.zen.service;

import com.ops.zen.entity.ZenSysRole;

import java.util.List;

public interface SysRoleService {
    List<ZenSysRole> list();

    void save(ZenSysRole role);

    void update(ZenSysRole role);

    void delete(Long pkRole);

    void assignMenus(Long pkRole, List<Long> menuIds);

    List<String> getMenuIdsByRoleId(Long pkRole);
}
