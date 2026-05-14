package com.ops.zen.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ops.zen.jdbc.annotation.Column;
import com.ops.zen.jdbc.annotation.Entity;
import com.ops.zen.jdbc.annotation.Id;
import com.ops.zen.json.*;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据源
 * @Date 2025-04-23 15:51:11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("zen_db_ds")
@Entity(name = "zen_db_ds")
public class ZenDbDs implements Serializable {
    /**
     * 主键
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    @TableId(value = "PK_DS", type = IdType.INPUT)
    @Id(auto = false)
    @Column("PK_DS")
    private Long pkDs;

    /**
     * 数据库类型
     */
    @Column("DB_TYPE")
    private String dbType;

    /**
     * 连接方式 1主机2URL
     */
    @Column("CONN_TYPE")
    private String connType;

    /**
     * 名称
     */
    @Column("NAME")
    private String name;

    /**
     * IP地址
     */
    @Column("HOST")
    private String host;

    /**
     * 端口号
     */
    @Column("PORT")
    private String port;

    /**
     * 数据库
     */
    @Column("DB_SCHEMA")
    private String dbSchema;

    /**
     * 链接地址
     */
    @Column("URL")
    private String url;

    /**
     * 用户名
     */
    @Column("USER_NAME")
    private String userName;

    /**
     * 密码
     */
    @Column("USER_PWD")
    private String userPwd;

    /**
     * 驱动
     */
    @Column("DRIVER")
    private String driver;

    /**
     * 最大连接数
     */
    @Column("CONN_MAX")
    private Double connMax;

    /**
     * 最小连接数
     */
    @Column("CONN_MIN")
    private Double connMin;

    /**
     * 描述
     */
    @Column("DES")
    private String des;

    /**
     * 是否系统数据
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    @Column("IS_SYS")
    private Byte isSys;

    /**
     * 是否锁定数据
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    @Column("IS_LOCK")
    private Byte isLock;

    /**
     * 创建人
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    @Column("PK_CREATEDBY")
    private Long pkCreatedby;

    /**
     * 创建时间
     */
    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    @Column("DT_CREATED")
    private LocalDateTime dtCreated;

    /**
     * 修改人
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    @Column("PK_MODIFIEDBY")
    private Long pkModifiedby;

    /**
     * 修改时间
     */
    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    @Column("DT_MODIFIED")
    private LocalDateTime dtModified;

    /**
     * 删除标志
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    @Column("DS")
    private Byte ds;

    /**
     * 版本
     */
    @Column("VERSION")
    private Double version;

    /**
     * 收藏时间
     */
    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    @Column("DT_FAVORITE")
    private LocalDateTime dtFavorite;

    /**
     * 标签(逗号分隔，如: 生产:primary,测试:warning)
     */
    @Column("TAGS")
    private String tags;
}
