package com.ops.zen.utils.pk;

import com.ops.zen.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 雪花主键生成
 *
 * @author xiaoyingnan
 *
 */
public class SnowPkGenerator {

    private static HashMap<String, AtomicInteger> tssCache = new HashMap<String, AtomicInteger>(1);

    private static final ReentrantLock lock = new ReentrantLock();

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");

    public static String SUFFIX = null;

    public static String generateCluster() {
        return generateInner();
    }

    public static String generate() {
        return generateCluster();
    }

    private static IdWorker worker = new IdWorker(1, 1, 1);

    public static Long generateSnow() {
        return worker.nextId();
    }

    @Deprecated
    public static String generateInner() {
        String timestamp = null;
        String inc = null;
        lock.lock();
        try {
            timestamp = sdf.format(new Date());
            AtomicInteger value = tssCache.get(timestamp);
            if (value == null) {
                tssCache.clear();
                tssCache.put(timestamp, new AtomicInteger(0));
                inc = "0";
            } else {
                inc = String.valueOf(value.addAndGet(1));
            }
        } finally {
            lock.unlock();
        }
        String suffix = SUFFIX == null ? "1" : SUFFIX;
        return timestamp + StringUtils.leftPad(inc, 4, '0') + suffix;
    }

    @Deprecated
    public static String generateInnerWithoutSuffix() {
        String timestamp = null;
        String inc = null;
        lock.lock();
        try {
            timestamp = sdf.format(new Date());
            AtomicInteger value = tssCache.get(timestamp);
            if (value == null) {
                tssCache.clear();
                tssCache.put(timestamp, new AtomicInteger(0));
                inc = "0";
            } else {
                inc = String.valueOf(value.addAndGet(1));
            }
        } finally {
            lock.unlock();
        }
        return timestamp + StringUtils.leftPad(inc, 4, '0');
    }

}
