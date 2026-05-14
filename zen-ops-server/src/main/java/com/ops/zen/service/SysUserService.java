package com.ops.zen.service;

import com.ops.zen.entity.ZenSysUser;

import java.util.List;

public interface SysUserService {
    List<ZenSysUser> getListByIds(List<Long> userIdList);

    List<ZenSysUser> list();

    List<ZenSysUser> listByKeyword(String keyword);

    void save(ZenSysUser user);

    void saveWithRoles(ZenSysUser user, List<Long> roleIds);

    void update(ZenSysUser user);

    void updateWithRoles(ZenSysUser user, List<Long> roleIds);

    void delete(Long pkUser);

    void assignRoles(Long pkUser, List<Long> roleIds);

    List<Long> getRoleIdsByUserId(Long pkUser);
}
