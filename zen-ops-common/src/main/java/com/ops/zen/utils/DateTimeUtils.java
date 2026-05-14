package com.ops.zen.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author xiaoyingnan
 * @version 2020/7/14 13:57
 * <文件说明>
 **/
public class DateTimeUtils {

    /**
     * 将yyyy-MM-dd HH:mm:ss格式的日期字符串转为LocalDateTime
     *
     * @param str 字符串
     * @return 时间
     */
    public static LocalDateTime parseLocalDateTime(String str) {
        return parseLocalDateTime(str, "yyyy-MM-dd HH:mm:ss");
    }

    public static LocalDateTime parseLocalDateTimeyyyyMMddHHmmss(String str) {
        return parseLocalDateTime(str, "yyyyMMddHHmmss");
    }

    public static LocalDateTime parseLocalDateTime(String str, String pattern) {
        return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseLocalDate(String str, String pattern) {
        return LocalDate.parse(str, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime currentLocalDateTime() {
        return LocalDateTime.now();
    }


    /**
     * 将LocalDateTime转为yyyy-MM-dd HH:mm:ss格式的字符串
     *
     * @param localDateTime
     * @return
     */
    public static String format(LocalDateTime localDateTime) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(localDateTime);
    }

    public static String currentYYYYMMDDHHMMSS() {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
    }

    public static String format(LocalDateTime localDateTime, String format) {
        return DateTimeFormatter.ofPattern(format).format(localDateTime);
    }

    public static String format(LocalDate localDate, String format) {
        return DateTimeFormatter.ofPattern(format).format(localDate);
    }

    public static long toMillis(String str) {
        LocalDateTime parse = LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return currentTimeMillis(parse);
    }

    public static String normalFormatDate(Date value) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
    }

    public static String currentNormalDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static long currentTimeMillis() {
        return currentTimeMillis(LocalDateTime.now());
    }

    public static long currentTimeMillis(LocalDateTime ldt) {
        //+8 修改为使用OffsetDateTime.now(ZoneId.systemDefault()).getOffset()获取offset 20210408
//        return ldt.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        return ldt.toInstant(defaultOffset()).toEpochMilli();
    }

    public static ZoneOffset defaultOffset() {
        return OffsetDateTime.now(ZoneId.systemDefault()).getOffset();
    }

    public static long currentTimeMillis(LocalDate ldt) {
        //+8 修改为使用OffsetDateTime.now(ZoneId.systemDefault()).getOffset()获取offset 20210408
//        return LocalDateTime.of(ldt, LocalTime.ofSecondOfDay(0l)).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        return LocalDateTime.of(ldt, LocalTime.ofSecondOfDay(0l)).toInstant(defaultOffset()).toEpochMilli();
    }

    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate asLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime asLocalDateTime(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }


    /**
     * 增加日期
     *
     * @param time   当前日期
     * @param number 增加的数量
     * @param field  单位/field为ChronoUnit.*
     * @return
     */
    public static LocalDateTime plus(LocalDateTime time, long number, TemporalUnit field) {
        return time.plus(number, field);
    }

    /**
     * 减少日期
     *
     * @param time   当前日期
     * @param number 减少的数量
     * @param field  单位/field为ChronoUnit.*
     * @return
     */
    public static LocalDateTime minus(LocalDateTime time, long number, TemporalUnit field) {
        return time.minus(number, field);
    }

    /**
     * 判断是否为周末
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static boolean isWeekend(Date date) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    /**
     * 判断是否为周末
     *
     * @return
     * @throws ParseException
     */
    public static boolean isWeekend(LocalDateTime localDateTime) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(asDate(localDateTime));
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    /**
     * 获取两个日期之间的所有日期
     *
     * @param start 开始时间 yyyy-MM-dd
     * @param end   结束时间 yyyy-MM-dd
     * @return list集合
     * @throws ParseException
     */
    public static List<String> getBetweenDates(String start, String end) throws ParseException {
        List<String> result = new ArrayList<String>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date start_date = sdf.parse(start);
        Date end_date = sdf.parse(end);
        Calendar tempStart = Calendar.getInstance();
        tempStart.setTime(start_date);
        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(end_date);
        while (tempStart.before(tempEnd) || tempStart.equals(tempEnd)) {
            result.add(sdf.format(tempStart.getTime()));
            tempStart.add(Calendar.DAY_OF_YEAR, 1);
        }
        return result;
    }

    public static int age(LocalDate date) {
        return LocalDate.now().getYear() - date.getYear();
    }


    public static long millisBetween(LocalDateTime begin, LocalDateTime end) {
        return Math.abs(DateTimeUtils.currentTimeMillis(end) - DateTimeUtils.currentTimeMillis(begin));
    }

    public static long millisBetween(long beginNano, long endNano) {
        return Math.abs(endNano - beginNano) / 1000000;
    }

    public static String format(Date value, String format) {
        return new SimpleDateFormat(format).format(value);
    }
}
