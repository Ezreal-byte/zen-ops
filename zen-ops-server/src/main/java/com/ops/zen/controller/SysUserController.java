package com.ops.zen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ops.zen.entity.ZenSysRole;
import com.ops.zen.entity.ZenSysRoleUser;
import com.ops.zen.entity.ZenSysUser;
import com.ops.zen.mapper.ZenSysRoleMapper;
import com.ops.zen.mapper.ZenSysRoleUserMapper;
import com.ops.zen.mapper.ZenSysUserMapper;
import com.ops.zen.service.SysUserService;
import com.ops.zen.utils.AESUtils;
import com.ops.zen.utils.HttpUtils;
import com.ops.zen.utils.UserContext;
import com.ops.zen.utils.map.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/sys/user")
@Slf4j
public class SysUserController {

    @Autowired
    private SysUserService service;

    @Autowired
    private ZenSysUserMapper sysUserMapper;

    @Autowired
    private ZenSysRoleUserMapper sysRoleUserMapper;

    @Autowired
    private ZenSysRoleMapper sysRoleMapper;

    @Value("${zen.user.default-password:admin123}")
    private String defaultPassword;

    @GetMapping("/page")
    public PageResult<ZenSysUser> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<ZenSysUser> list = service.listByKeyword(keyword);
        PageInfo<ZenSysUser> pageInfo = new PageInfo<>(list);
        // 补充角色中文名
        fillRoleNames(list);
        return PageResult.of(list, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
    }

    private void fillRoleNames(List<ZenSysUser> users) {
        if (users == null || users.isEmpty()) return;
        List<Long> userIds = users.stream().map(ZenSysUser::getPkUser).collect(java.util.stream.Collectors.toList());
        // 批量查角色关联
        QueryWrapper<ZenSysRoleUser> ruWrapper = new QueryWrapper<>();
        ruWrapper.in("pk_user", userIds);
        List<ZenSysRoleUser> roleUsers = sysRoleUserMapper.selectList(ruWrapper);
        if (roleUsers.isEmpty()) return;
        // 查角色名
        List<Long> roleIds = roleUsers.stream().map(ZenSysRoleUser::getPkRole).distinct().collect(java.util.stream.Collectors.toList());
        QueryWrapper<ZenSysRole> roleWrapper = new QueryWrapper<>();
        roleWrapper.in("pk_role", roleIds).select("pk_role", "name");
        List<ZenSysRole> roles = sysRoleMapper.selectList(roleWrapper);
        java.util.Map<Long, String> roleNameMap = roles.stream().collect(java.util.stream.Collectors.toMap(ZenSysRole::getPkRole, ZenSysRole::getName));
        // 按用户分组：同时填充roleNames和roleIds
        java.util.Map<Long, List<String>> userRoleNameMap = new java.util.HashMap<>();
        java.util.Map<Long, List<String>> userRoleIdMap = new java.util.HashMap<>();
        for (ZenSysRoleUser ru : roleUsers) {
            String roleName = roleNameMap.get(ru.getPkRole());
            if (roleName != null) {
                userRoleNameMap.computeIfAbsent(ru.getPkUser(), k -> new java.util.ArrayList<>()).add(roleName);
                userRoleIdMap.computeIfAbsent(ru.getPkUser(), k -> new java.util.ArrayList<>()).add(String.valueOf(ru.getPkRole()));
            }
        }
        for (ZenSysUser user : users) {
            user.setRoleNames(userRoleNameMap.getOrDefault(user.getPkUser(), java.util.Collections.emptyList()));
            user.setRoleIds(userRoleIdMap.getOrDefault(user.getPkUser(), java.util.Collections.emptyList()));
        }
    }

    @PostMapping("/save")
    public Object save(
            @RequestPart("form") ZenSysUser user,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "roleIds", required = false) String roleIds) throws Exception {
        // 检查用户名是否已存在
        QueryWrapper<ZenSysUser> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("user_name", user.getUserName());
        ZenSysUser existingUser = sysUserMapper.selectOne(checkWrapper);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 新增用户使用默认密码
        user.setPassword(AESUtils.encrypt(defaultPassword, AESUtils.AES_KEY));
        if (file != null && !file.isEmpty()) {
            user.setBlobHeader(file.getBytes());
        }
        service.saveWithRoles(user, parseRoleIds(roleIds));
        return user.getPkUser();
    }

    @PostMapping("/update")
    public String update(
            @RequestPart("form") ZenSysUser user,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "roleIds", required = false) String roleIds) throws Exception {
        // 编辑用户不修改密码
        user.setPassword(null);
        if (file != null && !file.isEmpty()) {
            user.setBlobHeader(file.getBytes());
        }
        service.updateWithRoles(user, parseRoleIds(roleIds));
        return "ok";
    }

    private List<Long> parseRoleIds(String roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return java.util.Arrays.stream(roleIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/delete/{pkUser}")
    public String delete(@PathVariable Long pkUser) {
        ZenSysUser user = sysUserMapper.selectById(pkUser);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getIsSys() != null && user.getIsSys() == 1) {
            throw new RuntimeException("系统数据无法删除");
        }
        service.delete(pkUser);
        return "ok";
    }

    @PostMapping("/assign-roles")
    public String assignRoles(@RequestBody Map<String, Object> params) {
        Long pkUser = Long.valueOf(params.get("pkUser").toString());
        List<?> roleIdsRaw = (List<?>) params.get("roleIds");
        List<Long> roleIds = roleIdsRaw.stream().map(o -> Long.valueOf(o.toString())).collect(java.util.stream.Collectors.toList());
        service.assignRoles(pkUser, roleIds);
        return "ok";
    }

    @GetMapping("/role-ids/{pkUser}")
    public List<Long> roleIds(@PathVariable Long pkUser) {
        return service.getRoleIdsByUserId(pkUser);
    }

    @PostMapping("/reset-password/{pkUser}")
    public String resetPassword(@PathVariable Long pkUser) {
        ZenSysUser user = sysUserMapper.selectById(pkUser);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(AESUtils.encrypt(defaultPassword, AESUtils.AES_KEY));
        sysUserMapper.updateById(user);
        return "ok";
    }

    @GetMapping("/header/{pkUser}")
    public void header(@PathVariable Long pkUser, HttpServletResponse response) throws IOException {
        ZenSysUser user = sysUserMapper.selectByPrimaryKeyWithBlobs(pkUser);
        if (user == null || user.getBlobHeader() == null) {
            response.setStatus(404);
            return;
        }
        HttpUtils.flushJpeg(response, user.getBlobHeader());
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        Long pkUser = UserContext.getUserContext().getPkUser();
        ZenSysUser user = sysUserMapper.selectById(pkUser);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        String oldEncrypt = AESUtils.encrypt(oldPassword, AESUtils.AES_KEY);
        if (!Objects.equals(user.getPassword(), oldEncrypt)) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(AESUtils.encrypt(newPassword, AESUtils.AES_KEY));
        sysUserMapper.updateById(user);
        return "ok";
    }
}
