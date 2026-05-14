package com.ops.zen.entity.request;

import lombok.Data;

/**
 * @author xyn
 * @date 2025/4/11 17:31
 * @description
 **/
@Data
public class LoginRequest {

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String args;

    private String password;
}
