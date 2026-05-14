package com.ops.zen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ops.zen.en.SshLoginTypeEn;
import com.ops.zen.entity.LoginUser;
import com.ops.zen.entity.ZenSsh;
import com.ops.zen.entity.response.ZenSshListResponse;
import com.ops.zen.mapper.ZenSshMapper;
import com.ops.zen.service.SshServerService;
import com.ops.zen.utils.Assert;
import com.ops.zen.utils.StringUtils;
import com.ops.zen.utils.en.EnUtils;
import com.ops.zen.utils.map.PageResult;
import com.ops.zen.utils.pk.SnowPkGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xyn
 * @date 2025/4/11 15:02
 * @description
 **/
@Slf4j
@Service
public class SshServerServiceImpl implements SshServerService {

    @Autowired
    ZenSshMapper mapper;

    @Override
    public String add(ZenSsh sshServer, LoginUser loginUser) {
        sshServer.setPkServer(SnowPkGenerator.generateSnow());
        sshServer.setPkCreatedby(loginUser.getPkUser());
        mapper.insert(sshServer);
        return null;
    }

    @Override
    public String delete(Long pkServer, LoginUser loginUser) {
        ZenSsh svr = mapper.selectById(pkServer);
        Assert.notNull(svr, "服务器不存在");
        Assert.isTrue(Objects.equals(svr.getPkCreatedby(), loginUser.getPkUser()), "无权删除他人创建的服务器");
        mapper.deleteById(pkServer);
        return null;
    }

    @Override
    public String update(ZenSsh sshServer, LoginUser loginUser) {
        ZenSsh exist = mapper.selectById(sshServer.getPkServer());
        Assert.notNull(exist, "服务器不存在");
        Assert.isTrue(Objects.equals(exist.getPkCreatedby(), loginUser.getPkUser()), "无权修改他人创建的服务器");
        sshServer.setPkModifiedby(loginUser.getPkUser());
        sshServer.setDtModified(LocalDateTime.now());
        mapper.updateById(sshServer);
        return null;
    }

    @Override
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
        wrapper.orderByDesc("DT_CREATED");
        PageHelper.startPage(Objects.isNull(pageNum) ? 1 : pageNum, Objects.isNull(pageSize) ? 20 : pageSize);
        List<ZenSsh> sshServers = mapper.selectList(wrapper);
        PageInfo<ZenSsh> pageInfo = new PageInfo<>(sshServers);

        List<ZenSshListResponse> collect = sshServers.stream().map(item -> {
            ZenSshListResponse row = new ZenSshListResponse();
            BeanUtils.copyProperties(item, row);
            row.setLoginTpName(EnUtils.getLabel(SshLoginTypeEn.class, item.getLoginTp()));
            return row;
        }).collect(Collectors.toList());
        return new PageResult<>(collect, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
    }

    @Override
    public ZenSshListResponse get(Long pkServer, LoginUser loginUser) {
        ZenSsh svr = mapper.selectById(pkServer);
        Assert.notNull(svr, "服务器不存在");
        Assert.isTrue(Objects.equals(svr.getPkCreatedby(), loginUser.getPkUser()), "无权查看他人创建的服务器");
        ZenSshListResponse row = new ZenSshListResponse();
        BeanUtils.copyProperties(svr, row);
        row.setLoginTpName(EnUtils.getLabel(SshLoginTypeEn.class, svr.getLoginTp()));
        return row;
    }

    @Override
    public ZenSsh get(Long pkServer) {
        return mapper.selectById(pkServer);
    }
}
