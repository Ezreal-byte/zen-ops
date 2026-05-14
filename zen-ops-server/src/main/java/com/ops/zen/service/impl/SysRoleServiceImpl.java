package com.ops.zen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ops.zen.entity.ZenSysMenuRole;
import com.ops.zen.entity.ZenSysRole;
import com.ops.zen.entity.ZenSysRoleUser;
import com.ops.zen.mapper.ZenSysMenuRoleMapper;
import com.ops.zen.mapper.ZenSysRoleMapper;
import com.ops.zen.mapper.ZenSysRoleUserMapper;
import com.ops.zen.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    private ZenSysRoleMapper sysRoleMapper;

    @Autowired
    private ZenSysMenuRoleMapper sysMenuRoleMapper;

    @Autowired
    private ZenSysRoleUserMapper sysRoleUserMapper;

    @Override
    public List<ZenSysRole> list() {
        return sysRoleMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public void save(ZenSysRole role) {
        sysRoleMapper.insert(role);
    }

    @Override
    public void update(ZenSysRole role) {
        sysRoleMapper.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long pkRole) {
        sysRoleMapper.deleteById(pkRole);
        // 删除角色菜单关联
        sysMenuRoleMapper.delete(new QueryWrapper<ZenSysMenuRole>().eq("pk_role", pkRole));
        // 删除角色用户关联
        sysRoleUserMapper.delete(new QueryWrapper<ZenSysRoleUser>().eq("pk_role", pkRole));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long pkRole, List<Long> menuIds) {
        // 先删除旧的关联
        sysMenuRoleMapper.delete(new QueryWrapper<ZenSysMenuRole>().eq("pk_role", pkRole));
        if (CollectionUtils.isEmpty(menuIds)) {
            return;
        }
        for (Long menuId : menuIds) {
            ZenSysMenuRole mr = ZenSysMenuRole.builder()
                    .pkMenu(menuId)
                    .pkRole(pkRole)
                    .build();
            sysMenuRoleMapper.insert(mr);
        }
    }

    @Override
    public List<String> getMenuIdsByRoleId(Long pkRole) {
        List<ZenSysMenuRole> list = sysMenuRoleMapper.selectList(
                new QueryWrapper<ZenSysMenuRole>().eq("pk_role", pkRole));
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(r -> String.valueOf(r.getPkMenu())).collect(Collectors.toList());
    }
}
