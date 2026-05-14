package com.ops.zen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ops.zen.json.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统菜单
 * @Date 2025-04-17 02:31:05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("zen_sys_menu")
public class ZenSysMenu implements Serializable {
    /**
     * 主键
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    @TableId(value = "PK_MENU", type = IdType.ASSIGN_ID)
    private Long pkMenu;

    /**
     * 父主键
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkParent;

    /**
     * 子菜单数目
     */
    @JsonIgnore
    private Integer subCount;

    /**
     * 菜单类型（功能节点、外链等）
     */
    private String nodeType;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 菜单组件路径/外链地址
     */
    private String url;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 排序
     */
    @JsonIgnore
    private Integer sortOrder;

    /**
     * 是否隐藏
     */
    @JsonIgnore
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte isHidden;

    /**
     * 是否系统数据
     */
    @JsonIgnore
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte isSys;

    /**
     * 是否锁定数据
     */
    @JsonIgnore
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte isLock;

    /**
     * 创建人
     */
    @JsonIgnore
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkCreatedby;

    /**
     * 创建时间
     */
    @JsonIgnore
    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    private LocalDateTime dtCreated;

    /**
     * 修改人
     */
    @JsonIgnore
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkModifiedby;

    /**
     * 修改时间
     */
    @JsonIgnore
    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    private LocalDateTime dtModified;

    /**
     * 删除标志
     */
    @JsonIgnore
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte ds;

    /**
     * 版本
     */
    @JsonIgnore
    private Double version;

    /**
     * 子菜单列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<ZenSysMenu> children;
}
