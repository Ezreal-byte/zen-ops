package com.ops.zen.dto;

import lombok.Data;

/**
 * Redis Key信息DTO
 */
@Data
public class RedisKeyDTO {
    
    /**
     * Key名称
     */
    private String key;
    
    /**
     * 类型(string/hash/list/set/zset)
     */
    private String type;
    
    /**
     * 过期时间(秒) -1表示永久
     */
    private Long ttl;
    
    /**
     * 过期时间(格式化文本) 如：1天2小时30分钟45秒
     */
    private String ttlFormat;
    
    /**
     * 值预览
     */
    private String preview;
    
    /**
     * 大小(字节)
     */
    private Long size;
    
    /**
     * 大小(格式化文本)
     */
    private String sizeText;
    
    /**
     * 是否有父级(包含冒号分隔的层级)
     */
    private Boolean hasParent;
    
    /**
     * 父级名称(冒号前的部分)
     */
    private String parent;
    
    /**
     * 子级名称(冒号后的部分)
     */
    private String child;
    
    /**
     * 是否为父级节点
     */
    private Boolean isParent;
    
    /**
     * 是否展开
     */
    private Boolean expanded;
    
    /**
     * 子节点数量
     */
    private Integer childCount;
    
    /**
     * 子节点列表
     */
    private java.util.List<RedisKeyDTO> children;
    
    /**
     * 格式化TTL为可读文本
     */
    public void setTtlFormatFromSeconds() {
        if (this.ttl == null || this.ttl < 0) {
            this.ttlFormat = "永久";
            return;
        }
        
        long totalSeconds = this.ttl;
        if (totalSeconds == 0) {
            this.ttlFormat = "已过期";
            return;
        }
        
        long days = totalSeconds / 86400;
        totalSeconds %= 86400;
        long hours = totalSeconds / 3600;
        totalSeconds %= 3600;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
        }
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append("秒");
        }
        
        this.ttlFormat = sb.toString();
    }
}
