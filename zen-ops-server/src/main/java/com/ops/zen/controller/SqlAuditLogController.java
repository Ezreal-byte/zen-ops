package com.ops.zen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ops.zen.entity.ZenDbExecLog;
import com.ops.zen.entity.ZenSysUser;
import com.ops.zen.mapper.ZenDbExecLogMapper;
import com.ops.zen.mapper.ZenSysUserMapper;
import com.ops.zen.sqlwindow.vo.SqlAuditLogVo;
import com.ops.zen.utils.map.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SQL审计日志
 */
@RestController
@RequestMapping("/sql/audit")
@Slf4j
public class SqlAuditLogController {

    @Autowired
    private ZenDbExecLogMapper execLogMapper;

    @Autowired
    private ZenSysUserMapper sysUserMapper;

    /**
     * 分页查询SQL执行日志（连表查询用户信息）
     */
    @GetMapping("/page")
    public PageResult<SqlAuditLogVo> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "100") Integer pageSize,
            @RequestParam(required = false) Long pkCreatedby,
            @RequestParam(required = false) String dbSchema,
            @RequestParam(required = false) String sqlType,
            @RequestParam(required = false) String execStatus,
            @RequestParam(required = false) String keyword) {

        PageHelper.startPage(pageNum, pageSize);

        // 使用自定义SQL连表查询
        List<SqlAuditLogVo> list = execLogMapper.selectLogsWithUser(
                pkCreatedby, dbSchema, sqlType, execStatus, keyword);

        PageInfo<SqlAuditLogVo> pageInfo = new PageInfo<>(list);

        return PageResult.of(list, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
    }

    /**
     * 获取操作人列表
     */
    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {
        List<ZenSysUser> users = sysUserMapper.selectList(null);
        return users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("pkUser", u.getPkUser());
            map.put("userName", u.getUserName());
            map.put("name", u.getName());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 获取数据库列表（去重）
     */
    @GetMapping("/schemas")
    public List<String> getSchemas() {
        QueryWrapper<ZenDbExecLog> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT DB_SCHEMA")
                .isNotNull("DB_SCHEMA")
                .ne("DB_SCHEMA", "");
        List<ZenDbExecLog> list = execLogMapper.selectList(wrapper);
        return list.stream()
                .map(ZenDbExecLog::getDbSchema)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
