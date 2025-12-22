package org.example.rentingmanagement.dto;

import lombok.Data;

/**
 * 分页请求DTO
 */
@Data
public class PageRequest {

    /**
     * 页码，从1开始
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向: asc, desc
     */
    private String sortOrder = "desc";

    /**
     * 获取偏移量
     */
    public Long getOffset() {
        return (long) (pageNum - 1) * pageSize;
    }
}
