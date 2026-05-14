package com.ops.zen.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ops.zen.en.SshLoginTypeEn;
import com.ops.zen.entity.LoginUser;
import com.ops.zen.entity.ZenSsh;
import com.ops.zen.entity.response.ZenSshListResponse;
import com.ops.zen.mapper.ZenSshMapper;
import com.ops.zen.ssh.vo.ServerInfoVo;
import com.ops.zen.utils.StringUtils;
import com.ops.zen.utils.UserContext;
import com.ops.zen.utils.en.EnUtils;
import com.ops.zen.utils.map.PageResult;
import com.ops.zen.utils.pk.SnowPkGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SSH服务器管理服务
 */
@Service
@Slf4j
public class SshService {

    @Autowired
    private ZenSshMapper sshServerMapper;

    /**
     * 添加服务器
     */
    public void add(ZenSsh sshServer) {
        sshServer.setPkServer(SnowPkGenerator.generateSnow());
        sshServer.setPkCreatedby(UserContext.getUserId());
        sshServer.setDtCreated(LocalDateTime.now());
        sshServerMapper.insert(sshServer);
    }

    /**
     * 删除服务器
     */
    public void delete(Long pkServer) {
        ZenSsh svr = sshServerMapper.selectById(pkServer);
        if (svr == null || !svr.getPkCreatedby().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权删除该服务器");
        }
        sshServerMapper.deleteById(pkServer);
    }

    /**
     * 更新服务器
     */
    public void update(ZenSsh sshServer) {
        sshServer.setPkModifiedby(UserContext.getUserId());
        sshServer.setDtModified(LocalDateTime.now());
        sshServerMapper.updateById(sshServer);
    }

    /**
     * 获取服务器详情
     */
    public ZenSsh get(Long pkServer) {
        ZenSsh svr = sshServerMapper.selectById(pkServer);
        if (svr == null || !svr.getPkCreatedby().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权查看该服务器");
        }
        return svr;
    }

    /**
     * 获取服务器列表
     */
    public PageResult<ZenSshListResponse> list(ZenSsh sshServer, Integer pageNum, Integer pageSize, LoginUser loginUser) {
        QueryWrapper<ZenSsh> wrapper = new QueryWrapper<>();
        wrapper.eq("PK_CREATEDBY", loginUser.getPkUser()).eq("DS", 0);
        if (StringUtils.isNotBlank(sshServer.getKeyword())) {
            wrapper.and(w -> w.like("NAME", sshServer.getKeyword()).or().like("IP", sshServer.getKeyword()));
        }
        if (StringUtils.isNotBlank(sshServer.getName())) {
            wrapper.like("NAME", sshServer.getName());
        }
        if (StringUtils.isNotBlank(sshServer.getIp())) {
            wrapper.like("IP", sshServer.getIp());
        }
        if (StringUtils.isNotBlank(sshServer.getPortSsh())) {
            wrapper.eq("PORT_SSH", sshServer.getPortSsh());
        }
        if (StringUtils.isNotBlank(sshServer.getUserName())) {
            wrapper.like("USER_NAME", sshServer.getUserName());
        }
        if (StringUtils.isNotBlank(sshServer.getLoginTp())) {
            wrapper.eq("LOGIN_TP", sshServer.getLoginTp());
        }
        if (StringUtils.isNotBlank(sshServer.getDes())) {
            wrapper.like("DES", sshServer.getDes());
        }
        // 排序：收藏的排在前面（按收藏时间降序），未收藏的按创建时间降序
        wrapper.orderByDesc("DT_FAVORITE").orderByDesc("DT_CREATED");
        PageHelper.startPage(Objects.isNull(pageNum) ? 1 : pageNum, Objects.isNull(pageSize) ? 20 : pageSize);
        List<ZenSsh> sshServers = sshServerMapper.selectList(wrapper);
        PageInfo<ZenSsh> pageInfo = new PageInfo<>(sshServers);

        List<ZenSshListResponse> collect = sshServers.stream().map(item -> {
            ZenSshListResponse row = new ZenSshListResponse();
            BeanUtils.copyProperties(item, row);
            row.setLoginTpName(EnUtils.getLabel(SshLoginTypeEn.class, item.getLoginTp()));
            return row;
        }).collect(Collectors.toList());
        return new PageResult<>(collect, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
    }

    /**
     * 测试SSH连接
     */
    public Map<String, Object> testConnection(Long pkServer) {
        ZenSsh svr = sshServerMapper.selectById(pkServer);
        if (svr == null) {
            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            map.put("message", "数据源不存在");
            return map;
        }
        return com.ops.zen.ssh.util.SshCommandUtil.testConnection(svr);
    }

    /**
     * 获取服务器系统信息
     */
    public ServerInfoVo getServerInfo(Long pkServer) {
        ZenSsh svr = sshServerMapper.selectById(pkServer);
        if (svr == null || !svr.getPkCreatedby().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权查看该服务器");
        }
        return com.ops.zen.ssh.util.SshCommandUtil.getServerInfo(svr);
    }

    /**
     * 获取服务器资源使用情况（CPU、内存、磁盘）
     */
    public ServerInfoVo.UsageVo getServerUsage(Long pkServer) {
        ZenSsh svr = sshServerMapper.selectById(pkServer);
        if (svr == null || !svr.getPkCreatedby().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权查看该服务器");
        }
        return com.ops.zen.ssh.util.SshCommandUtil.getServerUsage(svr);
    }

    /**
     * 收藏/取消收藏服务器
     */
    public void toggleFavorite(Long pkServer) {
        ZenSsh svr = sshServerMapper.selectById(pkServer);
        if (svr == null || !svr.getPkCreatedby().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权操作该服务器");
        }
        
        // 使用 UpdateWrapper 明确设置 null 值
        UpdateWrapper<ZenSsh> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("PK_SERVER", pkServer).eq("DS", 0);
        
        // 如果已收藏则取消，否则设置为收藏
        if (svr.getDtFavorite() != null) {
            // 取消收藏：设置为 NULL
            updateWrapper.set("DT_FAVORITE", null);
        } else {
            // 收藏：设置为当前时间
            updateWrapper.set("DT_FAVORITE", LocalDateTime.now());
        }
        updateWrapper.set("PK_MODIFIEDBY", UserContext.getUserId());
        updateWrapper.set("DT_MODIFIED", LocalDateTime.now());
        
        sshServerMapper.update(null, updateWrapper);
    }

    /**
     * 更新标签
     */
    public void updateTags(Long pkServer, String tags) {
        ZenSsh svr = sshServerMapper.selectById(pkServer);
        if (svr == null || !svr.getPkCreatedby().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权操作该服务器");
        }
        svr.setTags(tags);
        svr.setPkModifiedby(UserContext.getUserId());
        svr.setDtModified(LocalDateTime.now());
        sshServerMapper.updateById(svr);
    }
}
