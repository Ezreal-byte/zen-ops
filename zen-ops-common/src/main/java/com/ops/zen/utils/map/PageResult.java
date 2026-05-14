package com.ops.zen.utils.map;

import java.util.List;

/**
 * @Author xiaoyingnan
 * @Date 2020/06/04 09:11
 * @Description
 */
public class PageResult<T> {

    private List<T> list;

    private Integer pageNum;

    private Integer pageSize;

    private Long totalCount;

    private Integer size;

    private List<String> columns;

    private List<?> columnsMeta;

    public static final Integer MAX_RECORD_COUNT = 2000;

    public PageResult() {
    }

    public PageResult(List<T> list, Integer pageNum, Integer pageSize, Long totalCount) {
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = PageResult.MAX_RECORD_COUNT;
        }

        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.size = list.size();
    }

    public static <T> PageResult<T> of(List<T> list, Integer pageNum, Integer pageSize, Long totalCount) {
        return new PageResult<T>(list, pageNum, pageSize, totalCount);
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<?> getColumnsMeta() {
        return columnsMeta;
    }

    public void setColumnsMeta(List<?> columnsMeta) {
        this.columnsMeta = columnsMeta;
    }

}
