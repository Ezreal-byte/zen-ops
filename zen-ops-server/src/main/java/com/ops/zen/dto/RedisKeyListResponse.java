package com.ops.zen.dto;

import lombok.Data;

import java.util.List;

/**
 * Redis Key列表响应DTO
 */
@Data
public class RedisKeyListResponse {
    
    /**
     * Key列表
     */
    private List<RedisKeyDTO> list;
    
    /**
     * 总数
     */
    private Integer total;
    
    /**
     * 当前页码
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 总页数
     */
    private Integer pages;
    
    /**
     * 刷新时间
     */
    private String refreshTime;
}
