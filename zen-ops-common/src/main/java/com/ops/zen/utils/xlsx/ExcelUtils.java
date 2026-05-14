package com.ops.zen.utils.xlsx;

import com.ops.zen.utils.ex.Exceptions;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @Author xiaoyingnan
 * @Date 2020/7/9 10:04
 * @Description
 */
public abstract class ExcelUtils {

    public static Workbook getWorkBook(String filePath) {
        Workbook wb = null;
        if (filePath == null) {
            return null;
        }
        String ext = filePath.substring(filePath.lastIndexOf("."));
        try {
            InputStream is = new FileInputStream(filePath);
            if (".xls".equals(ext)) {
                wb = new HSSFWorkbook(is);
            } else if (".xlsx".equals(ext)) {
                wb = new XSSFWorkbook(is);
            } else {
                wb = null;
            };
            return wb;
        } catch (Exception e) {
            Exceptions.throwAsRuntimeException(e);
        }
        return null;
    }

    public static Workbook getWorkBook(InputStream inputStream, boolean isXlsx) {
        Workbook wb = null;
        try {
            if (isXlsx) {
                wb = new XSSFWorkbook(inputStream);
            } else {
                wb = new HSSFWorkbook(inputStream);
            }
            return wb;
        } catch (Exception e) {
            Exceptions.throwAsRuntimeException(e);
        }
        return null;
    }


    public static String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
//        System.out.println(index+ "   "+ row.getRowNum());
        return getCellValue(cell);
    }

    public static String getCellValue(Sheet sheet, int row, int column) {
        Cell c = sheet.getRow(row).getCell(column);
        return getCellValue(c);
    }

    public static String getCellValue(Cell cell) {
        if (cell == null) {
            System.err.println("单元格为空，被剪切的单元格可能有此问题，敲入空格解决问题");
            return "";
        }
        if (CellType.forInt(cell.getCellType()) == CellType.NUMERIC) return cell.getNumericCellValue()+"";
//        if (CellType.forInt(cell.getCellType()) == CellType.STRING) return cell.getNumericCellValue()+"";
        return cell.getStringCellValue();
    }


    public static void closeWorkBook(Workbook wb) {
        if (wb != null) {
            try {
                wb.close();
            } catch (Exception e) {
                Exceptions.throwAsRuntimeException(e);
            }
        }
    }
}
