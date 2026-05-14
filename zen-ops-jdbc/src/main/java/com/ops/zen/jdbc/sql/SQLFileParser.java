package com.ops.zen.jdbc.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class SQLFileParser {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    public static final String CTRL_START = "<!--";

    public static final String CTRL_END = "-->";

    private BufferedReader reader;

    private String sqlFileName;

    private String originContent;

    public SQLFileParser(String sqlFileName, String originContent) {
        this.sqlFileName = sqlFileName;
        this.originContent = originContent;
        reader = new BufferedReader(new StringReader(originContent));
    }

    public Map<String, SQLSource> parse() throws IOException {
        Map<String, SQLSource> sources = new HashMap<>();
        SQLSource source = null;
        do {
            source = next();
            if (source == null) {
                break;
            }
            String sqlId = source.getSqlId();
            SQLSource pre = sources.get(sqlId);
            if (pre != null) {
                throw new RuntimeException(String.format("已经存在【sqlId:%s，lineNumber:%d】，【sqlId:%s，lineNumber:%d】重复", pre.getSqlId(), pre.getLineNumber(),
                        sqlId, source.getLineNumber()));
            }
            sources.put(sqlId, source);
        } while (true);
        return sources;
    }

    private String strLastLine = null;

    private int lineNumber = 0;

    private SQLSource next() throws IOException {
        String strLine = null;
        if (strLastLine != null && strLastLine.equals(CTRL_START)) {

        } else {
            while ((strLine = nextLine()) != null && !strLine.equals(CTRL_START)) {
            }
            if (strLine == null) {
                return null;
            }
        }

        int line = 0;
        //读取sqlId
        String sqlId = null;
        while ((strLine = nextLine()) != null && !strLine.trim().equals(CTRL_END)) {
            if (strLine.trim().length() > 0) {
                sqlId = strLine;
                line = lineNumber;//sqlId所在的行号
                break;
            }
        }
        //读取comment
        StringBuilder sbComment = new StringBuilder();
        if (!strLine.trim().equals(CTRL_END)) {
            while ((strLine = nextLine()) != null && !strLine.trim().equals(CTRL_END)) {
                sbComment.append(strLine).append(LINE_SEPARATOR);
            }
        }
        //读取sql内容
        StringBuilder sbSqlContent = new StringBuilder();
        while ((strLine = nextLine()) != null && !strLine.trim().equals(CTRL_START)) {
            sbSqlContent.append(strLine).append(LINE_SEPARATOR);
        }
        strLastLine = strLine != null ? strLine.trim() : null;
        SQLSource source = new SQLSource(sqlFileName, sqlId, sbComment.toString(), sbSqlContent.toString(), line);
        return source;
    }

    private String nextLine() throws IOException {
        String s = reader.readLine();
        if (s != null) {
            ++lineNumber;
        }
        return s;
    }


}
