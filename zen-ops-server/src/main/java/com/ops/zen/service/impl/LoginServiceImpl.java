package com.ops.zen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ops.zen.en.BooleanEn;
import com.ops.zen.entity.LoginUser;
import com.ops.zen.entity.ZenSysUser;
import com.ops.zen.entity.request.LoginRequest;
import com.ops.zen.mapper.ZenSysUserMapper;
import com.ops.zen.service.LoginService;
import com.ops.zen.utils.AESUtils;
import com.ops.zen.utils.HttpUtils;
import com.ops.zen.utils.JwtUtils;
import com.ops.zen.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author xyn
 * @date 2025/4/11 17:32
 * @description
 **/
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    ZenSysUserMapper sysUserMapper;

    @Override
    public LoginUser login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        QueryWrapper<ZenSysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", loginRequest.getUserName());
        List<ZenSysUser> sysUsers = sysUserMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(sysUsers)) {
            wrapper = new QueryWrapper<>();
            wrapper.eq("email", loginRequest.getUserName());
            sysUsers = sysUserMapper.selectList(wrapper);
        }
        if (CollectionUtils.isEmpty(sysUsers)) {
            wrapper = new QueryWrapper<>();
            wrapper.eq("phone", loginRequest.getUserName());
            sysUsers = sysUserMapper.selectList(wrapper);
        }
        if (CollectionUtils.isEmpty(sysUsers)) {
            throw new RuntimeException("用户名或密码错误");
        }
        ZenSysUser sysUser = sysUsers.get(0);
        if (Objects.equals(sysUser.getIsLock(), BooleanEn.TRUE)) {
            throw new RuntimeException("用户已被锁定");
        }
        //验证密码
        String password = StringUtils.isNotEmpty(loginRequest.getArgs()) ? loginRequest.getArgs() : loginRequest.getPassword();
        String encrypt = AESUtils.encrypt(password, AESUtils.AES_KEY);
        if (!Objects.equals(sysUser.getPassword(), encrypt)) {
            throw new RuntimeException("用户名或密码错误");
        }
        //验证成功
        LoginUser loginUser = coverLoginUser(sysUser);
        String token = JwtUtils.getToken(loginUser);
        response.setHeader(JwtUtils.JWT_RESPONSE_HEADER_TOKEN, token);
        return loginUser;
    }

    private LoginUser coverLoginUser(ZenSysUser sysUser) {
        return LoginUser.builder()
                .pkUser(sysUser.getPkUser())
                .userName(sysUser.getUserName())
                .name(sysUser.getName())
                .email(sysUser.getEmail())
                .sex(sysUser.getSex())
                .department(sysUser.getDepartment())
                .phone(sysUser.getPhone())
//                .roles(sysUser.getRoles()) // todo 角色
                .build();
    }

    @Override
    public void header(Long pkUser, HttpServletResponse response) {
        ZenSysUser sysUser = sysUserMapper.selectByPrimaryKeyWithBlobs(pkUser);
        if (sysUser == null) {
            throw new RuntimeException("用户不存在");
        }
        byte[] blobHeader = Objects.isNull(sysUser.getBlobHeader()) ? new byte[0] : sysUser.getBlobHeader();
        try {
            HttpUtils.flushJpeg(response, blobHeader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
