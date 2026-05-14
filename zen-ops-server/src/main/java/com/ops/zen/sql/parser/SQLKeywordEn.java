package com.ops.zen.sql.parser;

/**
 * SQL关键字，一般是一个完整单元的标记
 */
public interface SQLKeywordEn {
    /*
    注释
     */
    String COMMENT_ONE = "--";
    String COMMENT_MULTI = "/*";

    /*
    DML
     */
    String DML_SELECT = "select";
    String DML_UPDATE = "update";
    String DML_INSERT = "insert";
    String DML_DELETE = "delete";
    String DML_TRUNCATE = "truncate";

    /*
     DDL
     */
    String DDL_ALTER = "alter";
    String DDL_DROP = "drop";
    String DDL_CREATE = "create";
    String DDL_COMMENT = "comment";
    String DDL_SET = "set";

    /*
    declare语法中可能出现的关键字
     */
    String ORCL_DECLARE = "declare";
    String ORCL_BEGIN = "begin";
    String ORCL_DBMS_LOB = "dbms_lob";
    String ORCL_END = "end";
}
