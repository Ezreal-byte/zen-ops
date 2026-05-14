package com.ops.zen.sql;

import com.ops.zen.cache.Pair;
import com.ops.zen.entity.ZenDbDs;
import com.ops.zen.jdbc.EasyRecord;
import com.ops.zen.jdbc.sql.EasyParams;
import com.ops.zen.mapper.ZenDbDsMapper;
import com.ops.zen.phy.PhyTableTool;
import com.ops.zen.phy.impl.TableTools;
import com.ops.zen.phy.meta.EtlFieldMeta;
import com.ops.zen.phy.vo.EtlFieldTpEn;
import com.ops.zen.sql.vo.FFSqlConnectionVO;
import com.ops.zen.utils.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ezreal
 * @date 2023/8/16 11:50
 * @description
 **/
public class FFSQLHelper {



    public static FFSqlConnectionVO generateConnectionVO(String connectionId, String pkDs, String opTp, int updateRows, Connection connection) {
//        CfgAppDs ds = IpfDaos.baseGet(DAOFactory.configDao(), CfgAppDs.class, Cnd.where(CfgAppDs.PKDS, "=", Long.parseLong(pkDs)).and(CfgAppDs.DS, "=", BooleanEn.FALSE));
        ZenDbDsMapper mapper = ApplicationContextUtils.get(ZenDbDsMapper.class);
//        ZenDbDs ds = Jdbcs.propertiesJdbc().get(ZenDbDs.class, Long.parseLong(pkDs));
        ZenDbDs ds = mapper.selectById(Long.parseLong(pkDs));
        FFSqlConnectionVO vo = new FFSqlConnectionVO();
        vo.setConnectionId(connectionId);
        vo.setConnection(connection);
        vo.setOpId(UUIDUtils.randomUUID());
        vo.setPkDs(pkDs);
        vo.setDsName(ds.getName());
        vo.setOpTp(opTp);
        vo.setUpdateRows(updateRows);
        vo.setDt(DateTimeUtils.currentYYYYMMDDHHMMSS());
        return vo;
    }



    public static Pair<Boolean, String> parseSqlSingleTableName(String sql) {
        // 将SQL语句转换为大写形式
        sql = sql.toUpperCase();
        // 去除前后空格，替换关键字之间的多个空格或换行符
        sql = sql.trim().replaceAll("\\s+", " ");
        // 使用正则表达式匹配单表查询语句的模式
        String pattern = "SELECT\\s+\\*\\s+FROM\\s+(\\w+)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(sql);
        if (matcher.find()) {
            // 匹配成功，解析出表名
            String tableName = matcher.group(1);
//            System.err.println("单表查询表名：" + tableName);
            return new Pair<>(true, tableName);
        }
//        else {
//             匹配失败，不是单表查询语句
//            System.err.println("不是单表查询语句");
//        }
        return new Pair<>(false, null);
    }

    public static PhyTableTool phyTableTool(String pkDs) {
//        ZenDbDs ds = Jdbcs.propertiesJdbc().get(ZenDbDs.class, Long.parseLong(pkDs));
        ZenDbDsMapper mapper = ApplicationContextUtils.get(ZenDbDsMapper.class);
//        ZenDbDs ds = Jdbcs.propertiesJdbc().get(ZenDbDs.class, Long.parseLong(pkDs));
        ZenDbDs ds = mapper.selectById(Long.parseLong(pkDs));
        Assert.notNull(ds, "datasource is null");
        //调用ETL的 TableTools 需要将类型转换
        return TableTools.tool(ds.getDbType());
    }


    public static List<Object> toListParams(EasyRecord record, List<EtlFieldMeta> fieldMetas) {
        List<Object> list = new ArrayList<>();
        //按照 fieldMetas 的顺序
        for (EtlFieldMeta fieldMeta : fieldMetas) {
            String fieldName = fieldMeta.getName().toLowerCase();
            String val = record.getString(fieldName);
            list.add(coverCellValue(val, fieldMeta.getType()));
        }
        return list;
    }

    public static void coverDateValue(List<EasyRecord> list) {
        for (EasyRecord easyRecord : list) {
            for (Map.Entry<String, Object> entry : easyRecord.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (Objects.nonNull(value)) {
                    if (value instanceof Timestamp) {
                        long time = ((Timestamp) value).getTime();
                        value = DateTimeUtils.normalFormatDate(new Date(time));
                        easyRecord.put(key, value);
                    } else if (value instanceof Date) {
                        value = DateTimeUtils.normalFormatDate((Date) value);
                        easyRecord.put(key, value);
                    }
                }
            }
        }
    }

    /**
     *
     * 数据类型转换
     * @param colType {@link EtlFieldTpEn}
     * */
    public static Object coverCellValue(String value, String colType) {
        if (StringUtils.isEmpty(value)) return null;
        try {
            Object val =  null;
            switch (colType) {
                case EtlFieldTpEn.NUMBER:
                    val = new BigDecimal(value);
                    break;
                case EtlFieldTpEn.DATETIME:
//                    val = Instant.ofEpochMilli(Long.parseLong(value))
//                            .atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
                    val = DateTimeUtils.parseLocalDateTime(value);
                    break;
                case EtlFieldTpEn.BLOB:
                    //TODO
                    val = Base64Utils.decode(value);
                    break;
                default: // STRING CLOB UNSUPPORT
                    val = value;
                    break;
            }

            return val;
        } catch (Exception e) {
            throw new RuntimeException(String.format("数据类型转换异常, value: %s,  toType: %s", value, colType), e);
        }
    }

    public static EasyParams toEasyParams(EasyRecord record, Map<String, String> fieldTypeMap) {
        EasyParams params = new EasyParams();
        //遍历行数据  kv是键值对
        for (Map.Entry<String, Object> kv : record.entrySet()) {
            String key = kv.getKey().toLowerCase();
            Object value = kv.getValue();
            if (fieldTypeMap.containsKey(key)) {
                String colType = fieldTypeMap.get(key);
                params.put(key, Objects.isNull(value) ? null : FFSQLHelper.coverCellValue(value.toString(), colType));
            }

        }
        return params;
    }



    public static String getFileTypeByBinary(byte[] decodedBytes) {
        // 获取文件头部的魔术数字
        byte[] magicNumbers = new byte[4];
        System.arraycopy(decodedBytes, 0, magicNumbers, 0, 4);
        // 根据魔术数字判断文件类型
        if (isPDF(magicNumbers)) {
            return "pdf";
        } else if (isDOC(magicNumbers)) {
            return "doc";
        } else if (isXLSX(magicNumbers)) {
            return "xlsx";
        } else if (isJPG(magicNumbers)) {
            return "jpeg";
        } else if (isTXT(magicNumbers)) {
            return "txt";
        } else if (isZIP(magicNumbers)) {
            return "zip";
        } else {
            return "";
        }
    }

    private static boolean isPDF(byte[] magicNumbers) {
        return magicNumbers[0] == 0x25 && magicNumbers[1] == 0x50 && magicNumbers[2] == 0x44 && magicNumbers[3] == 0x46;
    }

    private static boolean isDOC(byte[] magicNumbers) {
        return magicNumbers[0] == (byte) 0xD0 && magicNumbers[1] == (byte) 0xCF && magicNumbers[2] == (byte) 0x11 && magicNumbers[3] == (byte) 0xE0;
    }

    private static boolean isXLSX(byte[] magicNumbers) {
        return magicNumbers[0] == 0x50 && magicNumbers[1] == 0x4B && magicNumbers[2] == 0x03 && magicNumbers[3] == 0x04;
    }

    private static boolean isJPG(byte[] magicNumbers) {
        return magicNumbers[0] == (byte) 0xFF && magicNumbers[1] == (byte) 0xD8 && magicNumbers[2] == (byte) 0xFF && (magicNumbers[3] & 0xF0) == 0xE0;
    }

    private static boolean isTXT(byte[] magicNumbers) {
        return magicNumbers[0] == (byte) 0xEF && magicNumbers[1] == (byte) 0xBB && magicNumbers[2] == (byte) 0xBF;
    }

    private static boolean isZIP(byte[] magicNumbers) {
        return magicNumbers[0] == 0x50 && magicNumbers[1] == 0x4B && (magicNumbers[2] == 0x03 || magicNumbers[2] == 0x05) && (magicNumbers[3] == 0x04 || magicNumbers[3] == 0x06 || magicNumbers[3] == 0x08);
    }
}
