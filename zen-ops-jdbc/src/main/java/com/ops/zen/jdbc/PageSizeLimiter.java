package com.ops.zen.jdbc;

import com.ops.zen.utils.ex.Exceptions;
import org.slf4j.Logger;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class PageSizeLimiter {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(PageSizeLimiter.class);


    /**
     * 1）入参为空时返回PAGE_SIZE_LIMIT（2000）
     * 2）如果为几个特殊值（例如：PAGE_SIZE_MAX）直接返回特殊值
     * 3）如果size大于PAGE_SIZE_LIMIT（2000）返回PAGE_SIZE_LIMIT（2000）
     */
    public static Integer limitSize(Integer pageSize) {
        if (pageSize == null) {
            return Jdbc.PAGE_SIZE_LIMIT;
        }
        int i = pageSize.intValue();
        if (i == Jdbc.PAGE_SIZE_MAX
                || i == Jdbc.PAGE_SIZE_5000
                || i == Jdbc.PAGE_SIZE_10000
                || i == Jdbc.PAGE_SIZE_50000
                || i == Jdbc.PAGE_SIZE_LIMIT) {
            return pageSize;
        }
        if (i > Jdbc.PAGE_SIZE_LIMIT) {
            logger.warn("JDBC查询pageSize超限：{}，调用栈信息：{}", i, Exceptions.calledStackTrace(6));
            return Jdbc.PAGE_SIZE_LIMIT;
        }
        return pageSize;
    }
}
