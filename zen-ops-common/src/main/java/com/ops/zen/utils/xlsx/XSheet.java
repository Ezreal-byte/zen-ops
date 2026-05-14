package com.ops.zen.utils.xlsx;


import com.ops.zen.utils.Assert;

import java.util.*;

/**
 * XRow中的key为列英文名的小写形式
 *
 * @Author xiaoyingnan
 * @Date 2021/3/30 18:36
 * @Description
 */
public class XSheet {

    /**
     * XSheet名称
     */
    private String name;

    /**
     * 表头
     */
    private List<XHeader> headers = new ArrayList<>();

    /**
     * 小写英文名和中文名映射
     */
    private Map<String, String> enLowerCaseCnMap = new HashMap<>();

    /**
     * 小写英文名和原始英文名映射
     */
    private Map<String, String> enLowerCaseEnOriginMap = new HashMap<>();

    /**
     * 列索引和小写英文名映射
     */
    private Map<Integer, String> colIdxEnLowerCaseMap = new HashMap<>();

    /**
     * 表体内容
     */
    private List<XRow> bodyList = new ArrayList<>();

    /**
     * XRow的key为英文名小写
     *
     * @return
     */
    public List<XRow> getRows() {
        return bodyList;
    }

    public List<XHeader> getHeaders() {
        return headers;
    }

    /**
     * 添加表头，表头有中文名和英文名构成
     * 添加时构造三个映射关系
     *
     * @param cnName
     * @param enName
     * @return
     */
    public XSheet addHeader(String cnName, String enName) {
        headers.add(new XHeader(cnName, enName));
        enLowerCaseEnOriginMap.put(enName.toLowerCase(), enName);
        enLowerCaseCnMap.put(enName.toLowerCase(), cnName);
        colIdxEnLowerCaseMap.put(headers.size() - 1, enName.toLowerCase());
        return this;
    }

    public XSheet addHeader(XHeader header) {
        return addHeader(header.getCnName(), header.getEnName());
    }

    public XSheet addHeaders(List<XHeader> headers) {
        headers.forEach(h -> {
            addHeader(h.getCnName(), h.getEnName());
        });
        return this;
    }


    /**
     * key会被转为小写，key不能为空，value可以为空<br>
     * TODO 是否要校验row中的key包含在Headers中
     *
     * @param row
     * @return
     */
    public XSheet addRow(Map row) {
        Objects.requireNonNull(row);
        if (row instanceof XRow) {
            bodyList.add((XRow) row);
        } else {
            XRow r = new XRow();
            row.forEach((k, v) -> {
                Assert.notNull(k, "key不能为空");
                String key = k.toString().toLowerCase();
                String value = v == null ? null : v.toString();//空值处理
                r.put(key, new XCell(value));
            });
            bodyList.add(r);
        }
        return this;
    }

    /**
     * @param rows
     * @return
     */
    public XSheet addAllRow(List<Map> rows) {
        rows.forEach(row -> {
            addRow(row);
        });
        return this;
    }

    public XCell getBodyCell(int idxCol, int idxRow) {
        XRow xRow = bodyList.get(idxRow);
        XCell xCell = xRow.get(colIdxEnLowerCaseMap.get(idxCol));
        return xCell;
    }

    /**
     * 列数
     *
     * @return
     */
    public int colLength() {
        return headers.size();
    }

    /**
     * 除去表头意外的行数
     *
     * @return
     */
    public int rowLength() {
        return bodyList.size();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * 内部类，表头
     */
    public static class XHeader {

        private String cnName;

        private String enName;

        public XHeader(String cnName, String enName) {
            this.cnName = cnName;
            this.enName = enName;
        }

        public String getCnName() {
            return cnName;
        }

        public void setCnName(String cnName) {
            this.cnName = cnName;
        }

        public String getEnName() {
            return enName;
        }

        public void setEnName(String enName) {
            this.enName = enName;
        }

        /**
         * 按格式解析表头
         *
         * @param titleName 表头格式 中文名【英文名】
         * @return
         */
        public static XHeader parse(String titleName) {
            int idxBegin = titleName.indexOf("【");
            int idxEnd = titleName.lastIndexOf("】");
            if (idxBegin < 0 || idxEnd < 0) {
                throw new RuntimeException(String.format("%s不满足title格式", titleName));
            }
            return new XHeader(titleName.substring(0, idxBegin), titleName.substring(idxBegin + 1, idxEnd));
        }
    }
}
