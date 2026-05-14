package com.ops.zen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ops.zen.entity.ZenSysRoleUser;
import com.ops.zen.entity.ZenSysUser;
import com.ops.zen.mapper.ZenSysRoleUserMapper;
import com.ops.zen.mapper.ZenSysUserMapper;
import com.ops.zen.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private ZenSysUserMapper sysUserMapper;

    @Autowired
    private ZenSysRoleUserMapper sysRoleUserMapper;

    @Override
    public List<ZenSysUser> getListByIds(List<Long> userIdList) {
        QueryWrapper<ZenSysUser> wrapper = new QueryWrapper<>();
        wrapper.in("pk_user", userIdList);
        return sysUserMapper.selectList(wrapper);
    }

    @Override
    public List<ZenSysUser> list() {
        return sysUserMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public List<ZenSysUser> listByKeyword(String keyword) {
        QueryWrapper<ZenSysUser> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like("user_name", kw)
                    .or().like("name", kw)
                    .or().like("phone", kw)
                    .or().like("email", kw));
        }
        wrapper.orderByDesc("dt_created");
        // 不查 blob 字段，提升性能
        wrapper.select(ZenSysUser.class, info -> !info.getColumn().equals("blob_header"));
        return sysUserMapper.selectList(wrapper);
    }

    @Override
    public void save(ZenSysUser user) {
        sysUserMapper.insert(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithRoles(ZenSysUser user, List<Long> roleIds) {
        sysUserMapper.insert(user);
        if (!CollectionUtils.isEmpty(roleIds)) {
            for (Long roleId : roleIds) {
                ZenSysRoleUser ru = ZenSysRoleUser.builder()
                        .pkRole(roleId)
                        .pkUser(user.getPkUser())
                        .build();
                sysRoleUserMapper.insert(ru);
            }
        }
    }

    @Override
    public void update(ZenSysUser user) {
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithRoles(ZenSysUser user, List<Long> roleIds) {
        sysUserMapper.updateById(user);
        // 先删除旧角色关联，再插入新角色
        sysRoleUserMapper.delete(new QueryWrapper<ZenSysRoleUser>().eq("pk_user", user.getPkUser()));
        if (!CollectionUtils.isEmpty(roleIds)) {
            for (Long roleId : roleIds) {
                ZenSysRoleUser ru = ZenSysRoleUser.builder()
                        .pkRole(roleId)
                        .pkUser(user.getPkUser())
                        .build();
                sysRoleUserMapper.insert(ru);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long pkUser) {
        sysUserMapper.deleteById(pkUser);
        sysRoleUserMapper.delete(new QueryWrapper<ZenSysRoleUser>().eq("pk_user", pkUser));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long pkUser, List<Long> roleIds) {
        sysRoleUserMapper.delete(new QueryWrapper<ZenSysRoleUser>().eq("pk_user", pkUser));
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        for (Long roleId : roleIds) {
            ZenSysRoleUser ru = ZenSysRoleUser.builder()
                    .pkRole(roleId)
                    .pkUser(pkUser)
                    .build();
            sysRoleUserMapper.insert(ru);
        }
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long pkUser) {
        List<ZenSysRoleUser> list = sysRoleUserMapper.selectList(
                new QueryWrapper<ZenSysRoleUser>().eq("pk_user", pkUser));
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(ZenSysRoleUser::getPkRole).collect(Collectors.toList());
    }
}
