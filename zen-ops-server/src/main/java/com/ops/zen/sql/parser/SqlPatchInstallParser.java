package com.ops.zen.sql.parser;


import com.ops.zen.cache.Pair;
import com.ops.zen.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 优化方案：放弃StringBuilder的delete方法，使用自定义游标，在StringBuilder处理过程中不修改它自身的内容
 * 优化结果：40MB的数据，61066条SQL，解析需要1~2秒，耗内存大概100MB
 * <p>
 * 死循环问题已解决-观察、验证 2023/08/18
 *
 * @Author xyn
 * @Date 2023/6/08 13:07
 * @Description
 */
public class SqlPatchInstallParser {

    public static List<SinglePatchSql> parse(String sqlFileContent, String dbName, String fileName) {
        List<SinglePatchSql> rt = new ArrayList<>();
        List<Pair<Integer, String>> sqls = parse(fileName, sqlFileContent);
        for (int i = 0; i < sqls.size(); i++) {
            Pair<Integer, String> sql = sqls.get(i);
            SinglePatchSql patchSql = new SinglePatchSql();
            patchSql.setDbName(dbName);
            patchSql.setFileName(fileName);
            String sqlLower = sql.getValue().toLowerCase();
            if (sqlLower.startsWith(SQLKeywordEn.ORCL_DECLARE)) {
                int startIdx = i;
                int length = 1;
                while (true) {
                    try {
                        if (sqls.get(startIdx + length++).getValue().toLowerCase().startsWith(SQLKeywordEn.ORCL_END)
                            /*
                            TODO  && 不能等于其他关键字，比如SELECT，DELETE，UPDATE等
                             */
                        ) {
                            break;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(String.format("declare语句解析失败，非法的declare语句，行号：%d", sql.getKey()), e);
                    }
                }
                i += length - 1;
                List<String> origins = sqls.subList(startIdx, startIdx + length).stream().map(Pair::getValue).collect(Collectors.toList());
                patchSql.setOrigins(origins);
                // sql解析时会将所有的结束分号删除，对于declare语句通过jdbc执行来说，需要将原本的分号恢复，并且最后结尾为“end;”
                patchSql.setUnitSql(StringUtils.concate(origins, ";\n").concat(";"));
                patchSql.setKeyword(SQLKeywordEn.ORCL_DECLARE);
                patchSql.setSqlTp(SQLTpEn.DML);
                patchSql.setLineNum(sql.getKey());
                rt.add(patchSql);
            } else if (sqlLower.startsWith(SQLKeywordEn.COMMENT_MULTI)) {
                patchSql.setKeyword(SQLKeywordEn.COMMENT_MULTI);
                patchSql.setSqlTp(SQLTpEn.COMMENT);
                noneDeclareCommonSet(rt, sqls, i, sql, patchSql);
            } else if (sqlLower.startsWith(SQLKeywordEn.COMMENT_ONE)) {
                patchSql.setKeyword(SQLKeywordEn.COMMENT_ONE);
                patchSql.setSqlTp(SQLTpEn.COMMENT);
                noneDeclareCommonSet(rt, sqls, i, sql, patchSql);
            } else if (sqlLower.startsWith(SQLKeywordEn.DML_SELECT)) {
                patchSql.setKeyword(SQLKeywordEn.DML_SELECT);
                patchSql.setSqlTp(SQLTpEn.DML);
                noneDeclareCommonSet(rt, sqls, i, sql, patchSql);
            } else if (sqlLower.startsWith(SQLKeywordEn.DML_UPDATE)) {
                patchSql.setKeyword(SQLKeywordEn.DML_UPDATE);
                patchSql.setSqlTp(SQLTpEn.DML);
                noneDeclareCommonSet(rt, sqls, i, sql, patchSql);
            } else if (sqlLower.startsWith(SQLKeywordEn.DML_INSERT)) {
                patchSql.setKeyword(SQLKeywordEn.DML_INSERT);
                patchSql.setSqlTp(SQLTpEn.DML);
                noneDeclareCommonSet(rt, sqls, i, sql, patchSql);
            } else if (sqlLower.startsWith(SQLKeywordEn.DML_DELETE)) {
                patchSql.setKeyword(SQLKeywordEn.DML_DELETE);
                patchSql.setSqlTp(SQLTpEn.DML);
                noneDeclareCommonSet(rt, sqls, i, sql, patchSql);
            } else if (sqlLower.startsWith(SQLKeywordEn.DML_TRUNCATE)) {
                patchSql.setKeyword(SQLKeywordEn.DML_TRUNCATE);
                patchSql.setSqlTp(SQLTpEn.DML);
                noneDeclareCommonSet(rt, sqls, i, sql, patchSql);
            } else if (sqlLower.startsWith(SQLKeywordEn.DDL_ALTER)) {
                patchSql.setKeyword(SQLKeywordEn.DDL_ALTER);
                patchSql.setSqlTp(SQLTpEn.DDL);
                noneDeclareCommonSet(rt, sqls, i, sql, patchSql);
            } else if (sqlLower.startsWith(SQLKeywordEn.DDL_DROP)) {
                patchSql.setKeyword(SQLKeywordEn.DDL_DROP);
                patchSql.setSqlTp(SQLTpEn.DDL);
                noneDeclareCommonSet(rt, sqls, i, sql, patchSql);
            } else if (sqlLower.startsWith(SQLKeywordEn.DDL_CREATE)) {
                patchSql.setKeyword(SQLKeywordEn.DDL_CREATE);
                patchSql.setSqlTp(SQLTpEn.DDL);
                noneDeclareCommonSet(rt, sqls, i, sql, patchSql);
            } else if (sqlLower.startsWith(SQLKeywordEn.DDL_SET)) {
                patchSql.setKeyword(SQLKeywordEn.DDL_SET);
                patchSql.setSqlTp(SQLTpEn.DDL);
                noneDeclareCommonSet(rt, sqls, i, sql, patchSql);
            } else if (sqlLower.startsWith(SQLKeywordEn.DDL_COMMENT)) {
                patchSql.setKeyword(SQLKeywordEn.DDL_COMMENT);
                patchSql.setSqlTp(SQLTpEn.DDL);
                noneDeclareCommonSet(rt, sqls, i, sql, patchSql);
            } else {
                throw new RuntimeException(String.format("不支持的语句或关键字%s", sqlLower));
            }
        }
        for (int i = 0; i < rt.size(); i++) {
            setComments(rt.get(i), rt, i);
        }
        return rt;
    }

    /**
     * 解析出每一条SQL语句，包括：单行注释、多行注释、分号作为结束符的语句。
     *
     * @param fileName
     * @param sqlFileContent 原始sql文件中的文本内容
     * @return List【行号，SQL】
     */
    public static List<Pair<Integer, String>> parse(String fileName, String sqlFileContent) {
        AtomicInteger lineNumCursor = new AtomicInteger(1);
        String[] KEYWORDS = new String[]{"select", "update", "insert", "delete", "truncate", "alter", "drop", "create", "declare", "begin", "dbms_lob", "end", "--", "/*", "comment", "set"};
        StringBuilder sqlFileContentSb = new StringBuilder(sqlFileContent);
        List<Pair<Integer, String>> sqls = new ArrayList<>();
        // 从1开始，值>=内容长度时结束解析
        AtomicInteger idxCursorAtomic = new AtomicInteger(0);

        while (idxCursorAtomic.get() < sqlFileContentSb.length()) {
            int begin = idxCursorAtomic.get();
            Pair<Integer, String> oneSql = parseOneSql(KEYWORDS, sqlFileContentSb, idxCursorAtomic, lineNumCursor);
            if (begin == idxCursorAtomic.get()) { // parseOneSql调用后开始游标和结束游标不能一样，相同证明sql解析失败
                // 保证每次idxCursorAtomic都向前走（避免造成死循环），否则抛异常
                throw new RuntimeException(String.format("%s 解析失败，非法语句，行号：%d", fileName, lineNumCursor.get()));
            }
            if (oneSql == null) {
                continue;
            }
            String oneSqlValue = oneSql.getValue();
            if (StringUtils.isNotEmpty(oneSqlValue)) {
                sqls.add(oneSql);
            }
        }
        return sqls;
    }

    /**
     * 解析出一条SQL语句，包括：单行注释、多行注释、分号作为结束符的语句。
     *
     * @param keywords
     * @param sqlFileContentSb
     * @param idxCursorAtomic
     * @param lineNumberAtomic
     * @return
     */
    public static Pair<Integer, String> parseOneSql(String[] keywords, StringBuilder sqlFileContentSb, AtomicInteger idxCursorAtomic, AtomicInteger lineNumberAtomic) {
        for (String KEYWORD : keywords) {
            if (sqlFileContentSb.length() - idxCursorAtomic.get() + 1 <= KEYWORD.length()) {
                // 要保证和每一个关键字比较长度都更小，才能确定剩余字符串没有意义
                ignoreInvalidCharAndMoveCursor(sqlFileContentSb, idxCursorAtomic, lineNumberAtomic);
                continue;
            }
            // 删除无效的字符
            ignoreInvalidCharAndMoveCursor(sqlFileContentSb, idxCursorAtomic, lineNumberAtomic);
            if (idxCursorAtomic.get() >= sqlFileContentSb.length()) {
                return null;
            }

            String sqlKeyword = sqlFileContentSb.substring(idxCursorAtomic.get(), idxCursorAtomic.get() + KEYWORD.length());
            StringBuilder one = new StringBuilder();
            int idx = 0;
            if (!sqlKeyword.toLowerCase().equals(KEYWORD)) {
                // System.out.println(String.format("%s和关键字%s不匹配，继续尝试匹配下一个关键字", sqlKeyword, keyword));
                // TODO 死循环检测
                continue;
            }
            int lineNumInt = -1;
            if (KEYWORD.equals("--")) { // --------------------------------------单行注释解析
                lineNumInt = lineNumberAtomic.get();
                idx = KEYWORD.length() + idxCursorAtomic.get();
                one.append(sqlKeyword);
                while (true) {
                    char c = sqlFileContentSb.charAt(idx++);
                    if (idx >= sqlFileContentSb.length()) {
                        idxCursorAtomic.set(idx);
                        return new Pair(lineNumInt, one.toString()); // return one unit SQL，返回结果不带分号
                    }
                    lineNumAddedIfIsLinefeedChar(c, lineNumberAtomic);
                    if (c == '\r' || c == '\n') {
                        idxCursorAtomic.set(idx);
                        return new Pair(lineNumInt, one.toString()); // return single line comment unit
                    }
                    one.append(c);
                }
            } else if (KEYWORD.equals("/*")) { // ----------------------------------多行注释解析
                lineNumInt = lineNumberAtomic.get();
                idx = KEYWORD.length() + idxCursorAtomic.get();
                one.append(sqlKeyword);
                while (true) {
                    char c = sqlFileContentSb.charAt(idx++);
                    if (idx >= sqlFileContentSb.length()) {
                        idxCursorAtomic.set(idx);
                        return new Pair(lineNumInt, one.toString()); // return one unit SQL，返回结果不带分号
                    }
                    lineNumAddedIfIsLinefeedChar(c, lineNumberAtomic);
                    if (c == '/' && sqlFileContentSb.charAt(idx - 2) == '*') {
                        one.append(c);
                        idxCursorAtomic.set(idx);
                        return new Pair(lineNumInt, one.toString()); // return multiline line comment unit
                    }
                    one.append(c);
                }
            } else { // -----------------------------------------------------------------其他关键字解析 -- 解析出的sql不包括最后的分号
                lineNumInt = lineNumberAtomic.get();
                idx = KEYWORD.length() + idxCursorAtomic.get();
                one.append(sqlKeyword);
                Stack<Character> stack = new Stack<>();
                while (true) {
                    char c = sqlFileContentSb.charAt(idx++);
                    if (idx >= sqlFileContentSb.length()) {
                        idxCursorAtomic.set(idx);
                        return new Pair(lineNumInt, one.toString()); // return one unit SQL，返回结果不带分号
                    }
                    lineNumAddedIfIsLinefeedChar(c, lineNumberAtomic);
                    // 将该if后的其他if改为else if，解决：FF-5773 sql补丁文件SQL解析进入死循环问题分析解决
                    /*
                    单引号会成对出现，两个单引号中间出现分号不应解析为结束。
                    oracle中字符串中的单引号需要用两个单引号''进行转义，so 以下的解析可以做到sql的正确解析：insert into test (name) values('''');
                     */
                    if (stack.size() == 0 && c == '\'') {
                        stack.push(c);
                    } else if (stack.size() == 1 && c == '\'') {
                        stack.pop();
                    } else if (stack.size() == 0 && c == ';') {
                        // 一个sql单元解析完毕
                        idxCursorAtomic.set(idx);
                        return new Pair(lineNumInt, one.toString()); // return one unit SQL，返回结果不带分号
                    }
                    one.append(c);
                }
            }
        }
        return null;
    }

    /**
     * 删除无效字符
     *
     * @param sqlFileContentSb
     * @param idxCursorAtomic
     * @param lineNumberAtomic
     */
    public static void ignoreInvalidCharAndMoveCursor(StringBuilder sqlFileContentSb, AtomicInteger idxCursorAtomic, AtomicInteger lineNumberAtomic) {
        for (int i = idxCursorAtomic.get(); i < sqlFileContentSb.length(); i++) {
            // 避免删除多行注释的起始标记/*
            char c = sqlFileContentSb.charAt(i);
            if (c == '/' && i + 1 < (sqlFileContentSb.length() - 1) && sqlFileContentSb.charAt(i + 1) == '*') {
                return;
            }
            if (c == '\r' || c == '\n' || c == ' ' || c == '/' || c == '\t') {
                idxCursorAtomic.incrementAndGet();
                lineNumAddedIfIsLinefeedChar(c, lineNumberAtomic);
            } else {
                return;
            }
        }
        return;
    }

    private static void lineNumAddedIfIsLinefeedChar(char c, AtomicInteger lineNumberAtomic) {
        if (c == '\n') {
            lineNumberAtomic.incrementAndGet();
        }
    }


    public static void noneDeclareCommonSet(List<SinglePatchSql> rt, List<Pair<Integer, String>> sqls, int i, Pair<Integer, String> sql, SinglePatchSql patchSql) {
        patchSql.setLineNum(sql.getKey());
        patchSql.setUnitSql(sql.getValue());
        patchSql.setOrigins(sqls.subList(i, i + 1).stream().map(Pair::getValue).collect(Collectors.toList()));
        rt.add(patchSql);
    }

    private static void setComments(SinglePatchSql noneCommentSql, List<SinglePatchSql> rt, int i) {
        if (noneCommentSql.getSqlTp() == SQLTpEn.DML || noneCommentSql.getSqlTp() == SQLTpEn.DDL) {
            while (i-- >= 0) {
                if (i == -1) break;
                SinglePatchSql patchSql = rt.get(i);
                if (patchSql.getSqlTp() == SQLTpEn.COMMENT) { // 往前找可能有连续的多个注释，都赋给noneCommentSql
                    noneCommentSql.getComments().add(patchSql);
                } else { // 向上找，发现是非注释立刻结束
                    break;
                }
            }
        }
    }

}
