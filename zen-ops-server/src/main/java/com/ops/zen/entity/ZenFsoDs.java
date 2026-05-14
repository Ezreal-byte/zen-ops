package com.ops.zen.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ops.zen.json.*;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对象存储数据源
 * @Date 2026-04-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("zen_fso_ds")
public class ZenFsoDs implements Serializable {

    @TableId(value = "PK_FSO_DS", type = IdType.INPUT)
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkFsoDs;

    /**
     * 数据源名称
     */
    private String name;

    /**
     * 类型(MINIO/ALIYUN_OSS)
     */
    private String type;

    /**
     * 配置信息JSON(LONGTEXT存储)
     */
    private String clobConfig;

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
     * 是否默认数据源
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte isDefault;

    /**
     * 标签(逗号分隔，如: primary,warning)
     */
    private String tags;
}
