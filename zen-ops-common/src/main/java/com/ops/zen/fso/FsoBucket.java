package com.ops.zen.fso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xyn
 * @date 2026/4/27
 * @description 桶信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FsoBucket {

    /**
     * 桶名
     */
    private String name;

    /**
     * 创建时间
     */
    private String creationDate;

    /**
     * 区域/位置
     */
    private String region;

    /**
     * 对象数量
     */
    private Long objectCount;

    /**
     * 总大小(字节)
     */
    private Long totalSize;

    /**
     * 存储类型
     */
    private String storageClass;

    /**
     * 访问权限
     */
    private String accessPolicy;

    /**
     * 版本控制状态
     */
    private String versioningStatus;
}
