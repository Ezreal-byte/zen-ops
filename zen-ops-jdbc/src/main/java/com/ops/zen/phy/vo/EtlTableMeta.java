package com.ops.zen.phy.vo;

import com.ops.zen.phy.meta.EtlFieldMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * etl表元数据
 *
 * @Author xyn
 * @Date 2021/11/11 11:47
 * @Description
 */
public class EtlTableMeta {

    /**
     * 表名
     */
    private String name;

    /**
     * 描述，备注
     */
    private String des;

    /**
     * 列集合
     */
    private List<EtlFieldMeta> cols;

    /**
     * 主键列名
     */
    private String pkName;

    /**
     * 索引
     */
    private List<TableIndex> indexs;

    /**
     * 索引
     * 列的集合作索引
     */
    public static class TableIndex {

        /**
         * 组合索引的列集合
         */
        private Set<String> cols;

        /**
         * 索引名称，唯一索引以UNIQUE_开头，普通索引以IDX_开头
         */
        private String idxName;

        /**
         * 是否唯一约束索引
         */
        private boolean unique;

        /**
         * oracle时指定索引创建到的表空间名称
         */
        private String tableSpace;

        public TableIndex addIndex(String colName) {
            if (cols == null) {
                cols = new HashSet<>();
            }
            cols.add(colName);
            return this;
        }

        public boolean isUnique() {
            return unique;
        }

        public void setUnique(boolean unique) {
            this.unique = unique;
        }

        public Set<String> getCols() {
            return cols;
        }

        public void setCols(Set<String> cols) {
            this.cols = cols;
        }

        public String getIdxName() {
            return idxName;
        }

        public void setIdxName(String idxName) {
            this.idxName = idxName;
        }

        public String getTableSpace() {
            return tableSpace;
        }

        public void setTableSpace(String tableSpace) {
            this.tableSpace = tableSpace;
        }
    }


    //<editor-fold desc="set get">
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public List<EtlFieldMeta> getCols() {
        return cols;
    }

    public void setCols(List<EtlFieldMeta> cols) {
        this.cols = cols;
    }

    public String getPkName() {
        return pkName;
    }

    public void setPkName(String pkName) {
        this.pkName = pkName;
    }

    public List<TableIndex> getIndexs() {
        return indexs;
    }

    public void setIndexs(List<TableIndex> indexs) {
        this.indexs = indexs;
    }
    //</editor-fold>

}
