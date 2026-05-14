package com.ops.zen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ops.zen.entity.ZenSysMenu;
import com.ops.zen.entity.ZenSysMenuRole;
import com.ops.zen.entity.ZenSysRoleUser;
import com.ops.zen.mapper.ZenSysMenuMapper;
import com.ops.zen.mapper.ZenSysMenuRoleMapper;
import com.ops.zen.mapper.ZenSysRoleUserMapper;
import com.ops.zen.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl implements SysMenuService {

    @Autowired
    private ZenSysMenuMapper sysMenuMapper;

    @Autowired
    private ZenSysMenuRoleMapper sysMenuRoleMapper;

    @Autowired
    private ZenSysRoleUserMapper sysRoleUserMapper;

    @Override
    public List<ZenSysMenu> queryMenuTree() {
        List<ZenSysMenu> allMenus = sysMenuMapper.selectList(new QueryWrapper<ZenSysMenu>().orderByAsc("SORT_ORDER", "PK_MENU"));
        return buildTree(allMenus);
    }

    @Override
    public List<ZenSysMenu> queryAllMenus() {
        return sysMenuMapper.selectList(new QueryWrapper<ZenSysMenu>().orderByAsc("SORT_ORDER"));
    }

    @Override
    public List<ZenSysMenu> queryMenusByUserId(Long pkUser) {
        // 查询用户拥有的角色
        List<ZenSysRoleUser> roleUsers = sysRoleUserMapper.selectList(
                new QueryWrapper<ZenSysRoleUser>().eq("pk_user", pkUser));
        if (CollectionUtils.isEmpty(roleUsers)) {
            return Collections.emptyList();
        }
        List<Long> roleIds = roleUsers.stream().map(ZenSysRoleUser::getPkRole).collect(Collectors.toList());
        // 查询这些角色关联的菜单ID（去重）
        List<ZenSysMenuRole> menuRoles = sysMenuRoleMapper.selectList(
                new QueryWrapper<ZenSysMenuRole>().in("pk_role", roleIds));
        if (CollectionUtils.isEmpty(menuRoles)) {
            return Collections.emptyList();
        }
        List<Long> menuIds = menuRoles.stream()
                .map(ZenSysMenuRole::getPkMenu)
                .distinct()
                .collect(Collectors.toList());
        // 查询菜单详情
        List<ZenSysMenu> menus = sysMenuMapper.selectBatchIds(menuIds);
        menus.sort(Comparator
                .comparing(ZenSysMenu::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ZenSysMenu::getPkMenu));
        return buildTree(menus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMenu(ZenSysMenu menu) {
        if (menu.getPkParent() == null) {
            menu.setPkParent(0L);
        }
        if (menu.getSubCount() == null) {
            menu.setSubCount(0);
        }
        // 自动计算排序：同级下最大值+1
        if (menu.getSortOrder() == null) {
            Long pkParent = menu.getPkParent() == null ? 0L : menu.getPkParent();
            QueryWrapper<ZenSysMenu> wrapper = new QueryWrapper<>();
            wrapper.eq("PK_PARENT", pkParent).orderByDesc("SORT_ORDER").last("LIMIT 1");
            ZenSysMenu lastMenu = sysMenuMapper.selectOne(wrapper);
            menu.setSortOrder(lastMenu == null || lastMenu.getSortOrder() == null ? 1 : lastMenu.getSortOrder() + 1);
        }
        sysMenuMapper.insert(menu);
        // 更新父节点 subCount
        if (menu.getPkParent() != null && menu.getPkParent() != 0L) {
            ZenSysMenu parent = sysMenuMapper.selectById(menu.getPkParent());
            if (parent != null) {
                parent.setSubCount((parent.getSubCount() == null ? 0 : parent.getSubCount()) + 1);
                sysMenuMapper.updateById(parent);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(ZenSysMenu menu) {
        ZenSysMenu old = sysMenuMapper.selectById(menu.getPkMenu());
        if (old == null) {
            throw new RuntimeException("菜单不存在");
        }
        Long oldPid = old.getPkParent() == null ? 0L : old.getPkParent();
        Long newPid = menu.getPkParent() == null ? 0L : menu.getPkParent();
        boolean parentChanged = !oldPid.equals(newPid);

        if (parentChanged) {
            // 移到新父节点下，排序设为末尾
            QueryWrapper<ZenSysMenu> newWrapper = new QueryWrapper<>();
            newWrapper.eq("PK_PARENT", newPid).orderByDesc("SORT_ORDER").last("LIMIT 1");
            ZenSysMenu lastInNew = sysMenuMapper.selectOne(newWrapper);
            menu.setSortOrder(lastInNew == null || lastInNew.getSortOrder() == null ? 1 : lastInNew.getSortOrder() + 1);
        }

        sysMenuMapper.updateById(menu);

        // pkParent 变更时更新 subCount 和排序
        if (parentChanged) {
            // 旧父节点减一 & 重排旧父节点下兄弟排序
            if (oldPid != 0L) {
                ZenSysMenu oldParent = sysMenuMapper.selectById(oldPid);
                if (oldParent != null && oldParent.getSubCount() != null && oldParent.getSubCount() > 0) {
                    oldParent.setSubCount(oldParent.getSubCount() - 1);
                    sysMenuMapper.updateById(oldParent);
                }
            }
            reorderSiblings(oldPid);

            // 新父节点加一
            if (newPid != 0L) {
                ZenSysMenu newParent = sysMenuMapper.selectById(newPid);
                if (newParent != null) {
                    newParent.setSubCount((newParent.getSubCount() == null ? 0 : newParent.getSubCount()) + 1);
                    sysMenuMapper.updateById(newParent);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long pkMenu) {
        ZenSysMenu menu = sysMenuMapper.selectById(pkMenu);
        if (menu == null) {
            return;
        }
        // 如果有子菜单，先删除子菜单
        List<ZenSysMenu> children = sysMenuMapper.selectList(new QueryWrapper<ZenSysMenu>().eq("PK_PARENT", pkMenu));
        if (!CollectionUtils.isEmpty(children)) {
            throw new RuntimeException("请先删除子菜单");
        }
        sysMenuMapper.deleteById(pkMenu);
        // 更新父节点 subCount
        Long pkParent = menu.getPkParent();
        if (pkParent != null && pkParent != 0L) {
            ZenSysMenu parent = sysMenuMapper.selectById(pkParent);
            if (parent != null && parent.getSubCount() != null && parent.getSubCount() > 0) {
                parent.setSubCount(parent.getSubCount() - 1);
                sysMenuMapper.updateById(parent);
            }
        }
        // 重排同级兄弟排序
        reorderSiblings(menu.getPkParent() == null ? 0L : menu.getPkParent());
        // 删除关联的角色菜单关系
        sysMenuRoleMapper.delete(new QueryWrapper<ZenSysMenuRole>().eq("pk_menu", pkMenu));
    }

    @Override
    public List<ZenSysMenu> queryMenusByRoleId(Long roleId) {
        List<ZenSysMenuRole> list = sysMenuRoleMapper.selectList(
                new QueryWrapper<ZenSysMenuRole>().eq("pk_role", roleId));
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<Long> menuIds = list.stream().map(ZenSysMenuRole::getPkMenu).collect(Collectors.toList());
        return sysMenuMapper.selectBatchIds(menuIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveMenu(Long pkMenu, String direction) {
        ZenSysMenu menu = sysMenuMapper.selectById(pkMenu);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }
        Long pkParent = menu.getPkParent() == null ? 0L : menu.getPkParent();
        // 查询同级兄弟节点，按排序升序
        QueryWrapper<ZenSysMenu> wrapper = new QueryWrapper<>();
        wrapper.eq("PK_PARENT", pkParent).orderByAsc("SORT_ORDER");
        List<ZenSysMenu> siblings = sysMenuMapper.selectList(wrapper);
        int idx = -1;
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getPkMenu().equals(pkMenu)) {
                idx = i;
                break;
            }
        }
        if (idx < 0) return;
        int targetIdx = "up".equals(direction) ? idx - 1 : idx + 1;
        if (targetIdx < 0 || targetIdx >= siblings.size()) return;
        // 交换 sortOrder
        ZenSysMenu target = siblings.get(targetIdx);
        Integer curSort = menu.getSortOrder();
        Integer targetSort = target.getSortOrder();
        menu.setSortOrder(targetSort);
        target.setSortOrder(curSort);
        sysMenuMapper.updateById(menu);
        sysMenuMapper.updateById(target);
    }

    /**
     * 重排同级兄弟节点的 sortOrder，使其连续递增（1, 2, 3...）
     */
    private void reorderSiblings(Long pkParent) {
        QueryWrapper<ZenSysMenu> wrapper = new QueryWrapper<>();
        wrapper.eq("PK_PARENT", pkParent).orderByAsc("SORT_ORDER");
        List<ZenSysMenu> siblings = sysMenuMapper.selectList(wrapper);
        for (int i = 0; i < siblings.size(); i++) {
            ZenSysMenu s = siblings.get(i);
            if (s.getSortOrder() == null || s.getSortOrder() != i + 1) {
                s.setSortOrder(i + 1);
                sysMenuMapper.updateById(s);
            }
        }
    }

    private List<ZenSysMenu> buildTree(List<ZenSysMenu> menus) {
        if (CollectionUtils.isEmpty(menus)) {
            return Collections.emptyList();
        }
        Map<Long, ZenSysMenu> map = new LinkedHashMap<>();
        for (ZenSysMenu menu : menus) {
            menu.setChildren(new ArrayList<>());
            map.put(menu.getPkMenu(), menu);
        }
        List<ZenSysMenu> tree = new ArrayList<>();
        for (ZenSysMenu menu : menus) {
            Long pkParent = menu.getPkParent() == null ? 0L : menu.getPkParent();
            if (pkParent == 0L) {
                tree.add(menu);
            } else {
                ZenSysMenu parent = map.get(pkParent);
                if (parent != null) {
                    parent.getChildren().add(menu);
                } else {
                    tree.add(menu);
                }
            }
        }
        return tree;
    }
}
