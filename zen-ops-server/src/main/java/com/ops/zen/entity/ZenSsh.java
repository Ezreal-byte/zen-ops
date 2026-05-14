package com.ops.zen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ops.zen.json.*;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务器配置表
 * @Date 2025-04-17 02:31:05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("zen_ssh")
public class ZenSsh implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "PK_SERVER", type = IdType.INPUT)
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkServer;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String des;

    /**
     * 服务器IP地址
     */
    private String ip;

    /**
     * SSH端口号
     */
    private String portSsh;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String userPwd;

    /**
     * 登录类型(密码/私钥)
     */
    private String loginTp;

    /**
     * 私钥
     */
    private String prvKey;

    /**
     * 私钥密码
     */
    private String prvKeyPasswd;

    /**
     * 默认进入目录
     */
    private String initPath;

    /**
     * 是否系统数据
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte isSys;

    /**
     * 是否锁定数据
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte isLock;

    /**
     * 创建人
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkCreatedby;

    /**
     * 创建时间
     */
    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    private LocalDateTime dtCreated;

    /**
     * 修改人
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkModifiedby;

    /**
     * 修改时间
     */
    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    private LocalDateTime dtModified;

    /**
     * 删除标志
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte ds;

    /**
     * 版本
     */
    private Double version;

    /**
     * 搜索关键词
     */
    @TableField(exist = false)
    private String keyword;

    /**
     * 收藏时间
     */
    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    private LocalDateTime dtFavorite;

    /**
     * 标签(逗号分隔，如: 生产:primary,测试:warning)
     */
    private String tags;
}
