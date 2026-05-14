package com.ops.zen.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ops.zen.json.Long2StringDeserializer;
import com.ops.zen.json.Long2StringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xyn
 * @date 2025/4/11 15:10
 * @description
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    /**
     * 主键
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkUser;

    /**
     * 用户名
     */
    private String userName;


    /**
     * 姓名
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机
     */
    private String phone;

    /**
     * 性别
     */
    private String sex;

    /**
     * 部门
     */
    private String department;

    /**
     * 角色
     */
    private List<String> roles;

    public LoginUser(Map<String, String> map) {
        this.pkUser = Long.parseLong(map.get("pkUser"));
        this.userName = map.get("userName");
        this.name = map.get("name");
        this.email = map.get("email");
        this.sex = map.get("sex");
        this.department = map.get("department");
        this.roles = JSON.parseArray(map.get("roles"), String.class);
    }

    public Map<String, String> toMap() {
        //将this转为map
        Map<String, String> map = new HashMap<>();
        map.put("pkUser", this.pkUser.toString());
        map.put("userName", this.userName);
        map.put("name", this.name);
        map.put("email", this.email);
        map.put("sex", this.sex);
        map.put("department", this.department);
        map.put("roles", JSON.toJSONString(this.roles));
        return map;
    }
}
