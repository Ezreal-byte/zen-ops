package com.ops.zen.fso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xyn
 * @date 2026/4/27
 * @description 对象信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FsoObject {

    /**
     * 对象Key
     */
    private String key;

    /**
     * 对象大小(字节)
     */
    private Long size;

    /**
     * 最后修改时间
     */
    private String lastModified;

    /**
     * 是否目录
     */
    private Boolean isDir;

    /**
     * ETag
     */
    private String etag;

    /**
     * 存储类型 (如 STANDARD, GLACIER 等)
     */
    private String storageClass;

    /**
     * Content-Type
     */
    private String contentType;

    /**
     * 所属桶名
     */
    private String bucketName;
}
