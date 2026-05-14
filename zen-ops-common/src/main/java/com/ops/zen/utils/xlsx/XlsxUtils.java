package com.ops.zen.utils.xlsx;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * TODO 挪出一个独立的工程来做Excel或Pdf等的处理
 * 遇到空值null，对应值处理为null，没有处理成""（XSheet和Excel中空值都被处理称空字符串），不知是否有问题
 *
 * @Author xiaoyingnan
 * @Date 2021/3/30 19:53
 * @Description
 */
public class XlsxUtils {

    private static final Logger logger = LoggerFactory.getLogger(XlsxUtils.class);

    /**
     * 将XSheet写入EXcel
     *
     * @param xsheet
     * @param outputStream
     * @throws Exception
     */
    public static void writeExcelByXSheet(XSheet xsheet, OutputStream outputStream) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        writeWbSheetByXSheet(wb, xsheet);
        wb.write(outputStream);
        ExcelUtils.closeWorkBook(wb);
    }

    public static XSSFWorkbook writeExcelByXSheet(List<XSheet> xsheets) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        for (XSheet xsheet : xsheets) {
            writeWbSheetByXSheet(wb, xsheet);
        }
        return wb;
    }

    public static void writeWbSheetByXSheet(XSSFWorkbook wb, XSheet xsheet) {
        XSSFSheet sheetAt = wb.createSheet(xsheet.getName() != null ? xsheet.getName() : "Sheet0");
        //单元格样式
        XSSFCellStyle bodyCellStyle = getBodyXssfCellStyle(wb);
        XSSFCellStyle titleCellStyle = getTitleXssfCellStyle(wb);
        //写title
        XSSFRow rowHeader = sheetAt.createRow(0);
        List<XSheet.XHeader> headers = xsheet.getHeaders();
        for (int i = 0; i < headers.size(); i++) {
            XSSFCell cell = rowHeader.createCell(i, CellType.STRING);
            //begin 样式
            //样式需在赋值之前做
            cell.setCellStyle(titleCellStyle);
            //end 样式
            String cnName = headers.get(i).getCnName();
            String enName = headers.get(i).getEnName();
//            String titleName = String.format("%s【%s】", cnName, enName);
            cell.setCellValue(cnName);
        }
        //写内容
        for (int i = 0; i < xsheet.rowLength(); i++) {
            XSSFRow row = sheetAt.createRow(i + 1);
            for (int j = 0; j < xsheet.colLength(); j++) {
                XSSFCell cell = row.createCell(j);
                cell.setCellType(CellType.STRING);
                //begin 样式
                cell.setCellStyle(bodyCellStyle);
                //end 样式
                XCell bodyCell = xsheet.getBodyCell(j, i);

                String str = bodyCell == null ? "" : bodyCell.getValue();
                if (str != null && str.length() > 32767) {//最大长度不超过32767
                    logger.warn("单元格数据超长");
                    logger.debug(str);
                    str = str.substring(0, 32767);
                }
                cell.setCellValue(str);
            }
        }
    }

    private static XSSFCellStyle getBodyXssfCellStyle(XSSFWorkbook wb) {
        XSSFCellStyle cellStyle = wb.createCellStyle();
//        cellStyle.setBorderBottom(BorderStyle.THIN);//先style在color，否则下边框颜色不生效
//        cellStyle.setBottomBorderColor(new XSSFColor(new Color(0, 0, 0)));
//        cellStyle.setBorderTop(BorderStyle.THIN);//先style在color，否则下边框颜色不生效
//        cellStyle.setTopBorderColor(new XSSFColor(new Color(0, 0, 0)));
//        cellStyle.setBorderLeft(BorderStyle.THIN);//先style在color，否则下边框颜色不生效
//        cellStyle.setLeftBorderColor(new XSSFColor(new Color(0, 0, 0)));
//        cellStyle.setBorderRight(BorderStyle.THIN);//先style在color，否则下边框颜色不生效
//        cellStyle.setRightBorderColor(new XSSFColor(new Color(0, 0, 0)));
        return cellStyle;
    }

    private static XSSFCellStyle getTitleXssfCellStyle(XSSFWorkbook wb) {
        XSSFCellStyle cellStyle = wb.createCellStyle();
//            cellStyle.cloneStyleFrom(cell.getCellStyle());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);//先pattern再foregroundColor，否则前景色不生效
        cellStyle.setFillForegroundColor(new XSSFColor(new Color(190, 255, 75)));
        XSSFFont font = wb.createFont();
//            font.setFontName("Times New Roman");
        font.setBold(true);
        cellStyle.setFont(font);

        cellStyle.setBorderBottom(BorderStyle.THIN);//先style在color，否则下边框颜色不生效
        cellStyle.setBottomBorderColor(new XSSFColor(new Color(0, 0, 0)));
        cellStyle.setBorderTop(BorderStyle.THIN);//先style在color，否则下边框颜色不生效
        cellStyle.setTopBorderColor(new XSSFColor(new Color(0, 0, 0)));
        cellStyle.setBorderLeft(BorderStyle.THIN);//先style在color，否则下边框颜色不生效
        cellStyle.setLeftBorderColor(new XSSFColor(new Color(0, 0, 0)));
        cellStyle.setBorderRight(BorderStyle.THIN);//先style在color，否则下边框颜色不生效
        cellStyle.setRightBorderColor(new XSSFColor(new Color(0, 0, 0)));
        return cellStyle;
    }

    /**
     * 将sql返回的结果写入Excel，
     * 写入Excel中的表体数据列数<=header的列数
     *
     * @param headers      可为空，如果为空或者size==0，使用sql查询结果的字段名（小写）作为表头；表头的英文名必须和sql中查询出的字段名、别名相等（全部toLowerCase或toUpperCase以后进行比较)
     * @param dao
     * @param sql
     * @param sqlParams    可为空
     * @param sqlVars      可为空
     * @param pageNum
     * @param pageSize
     * @param outputStream
     * @throws Exception
     */
//    public static void writeExcelBySql(List<XSheet.XHeader> headers, Dao dao, String sql, SqlParams sqlParams, SqlVars sqlVars, int pageNum, int pageSize, OutputStream outputStream) throws Exception {
//        XSheet xSheet = getXSheetBySql(headers, dao, sql, sqlParams, sqlVars, pageNum, pageSize);
//        if (xSheet == null) return;
//        writeExcelByXSheet(xSheet, outputStream);
//    }
//
//    public static XSheet getXSheetBySql(List<XSheet.XHeader> headers, Dao dao, String sql, SqlParams sqlParams, SqlVars sqlVars, int pageNum, int pageSize) {
//        PageResult<Map> mapPageResult = IpfDaos.baseQueryMapsBySqlWithExpression(dao, sql, sqlParams, sqlVars, pageNum, pageSize);
//        List<Map> list = mapPageResult.getList();
//        if (list.size() == 0) {
//            return null;
//        }
//        Map map = list.get(0);
//        XSheet xSheet = new XSheet();
//        if (headers == null || headers.size() == 0) {
//            map.forEach((k, v) -> {
//                xSheet.addHeader(k.toString(), k.toString());
//            });
//        } else {
//            headers.forEach(h -> {
//                xSheet.addHeader(h.getCnName(), h.getEnName());
//            });
//        }
//        xSheet.addAllRow(list);
//        return xSheet;
//    }

    /**
     * 将Excel的Sheet读取为XSheet
     *
     * @param inputStream
     * @param sheetName
     * @return
     */
    public static XSheet readExcel(InputStream inputStream, String sheetName) {
        Workbook workBook = ExcelUtils.getWorkBook(inputStream, true);
        Sheet sheet = workBook.getSheet(sheetName);
        XSheet xSheet = new XSheet();
        //解析title
        Row row = sheet.getRow(0);
        int idxCol = 0;
        Cell cell = row.getCell(idxCol++);
        while (cell != null) {
            String titleName = ExcelUtils.getCellValue(cell);
            XSheet.XHeader xHeader = XSheet.XHeader.parse(titleName);
            xSheet.addHeader(xHeader.getCnName(), xHeader.getEnName());
            cell = row.getCell(idxCol++);
        }
        //解析内容
        int rowIdx = 1;
        row = sheet.getRow(rowIdx++);
        while (row != null) {
            //解析row，放入xSheet
//            System.err.println(row.getCell(1) + "      "+ row.getCell(3));
            xSheet.addRow(rowToMap(row, xSheet.getHeaders()));
            row = sheet.getRow(rowIdx++);
        }
        ExcelUtils.closeWorkBook(workBook);//关闭
        return xSheet;
    }

    /**
     * Excel row转为Map
     *
     * @param row
     * @param headers
     * @return
     */
    public static Map rowToMap(Row row, List<XSheet.XHeader> headers) {
        Map m = new HashMap();
        for (int i = 0; i < headers.size(); i++) {
            m.put(headers.get(i).getEnName(), ExcelUtils.getCellValue(row, i));
        }
        return m;
    }

    /**
     * XSheet转为PfObjects key为XHeader对应的原始英文名
     *
     * @param xSheet
     * @return
     */
//    public static List<PfObject> xSheetToPfObjectList(XSheet xSheet) {
//        List<XSheet.XHeader> headers = xSheet.getHeaders();
//        List<PfObject> list = new ArrayList<>();
//        for (int j = 0; j < xSheet.rowLength(); j++) {
//            PfObject pf = new PfObject();
//            for (int i = 0; i < headers.size(); i++) {
//                XCell bodyCell = xSheet.getBodyCell(i, j);
//                pf.put(headers.get(i).getEnName(), bodyCell == null ? null : bodyCell.getValue());//空值处理
//            }
//            list.add(pf);
//        }
//        return list;
//    }

    private static Map xRowToMap(XRow r, List<XSheet.XHeader> headers, BiFunction<String, Object, Object> convertFunc) {
        Map<String, Object> m = new HashMap<>();
        r.forEach((k, v) -> {
            if (convertFunc != null) {
                Object vv = convertFunc.apply(k, v == null ? v : v.getValue());
                m.put(k, vv);
            } else {
                m.put(k, v == null ? v : v.getValue());
            }
        });
        return m;
    }


}
