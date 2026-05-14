package com.ops.zen;

import com.alibaba.fastjson.JSON;
import com.ops.zen.ds.DSConfig;
import com.ops.zen.ds.DataSourceFactory;
import com.ops.zen.ds.DataSourceFromEn;
import com.ops.zen.entity.ZenSysUser;
import com.ops.zen.jdbc.EasyJdbc;
import com.ops.zen.jdbc.Jdbc;
import com.ops.zen.jdbc.sql.EasyParams;
import com.ops.zen.jdbc.sql.EasyVars;
import com.ops.zen.utils.Jdbcs;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

/**
 * @author xyn
 * @date 2025/4/11 18:53
 * @description
 **/
@SpringBootTest
public class EasyJdbcTest2 {



    @Test
    public void test() throws IOException {
        DSConfig dsConfig = new DSConfig(
                "测试",
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://localhost:3306/pixel_grid?useUnicode=true&characterEncoding=utf8&autoReconnect=true&serverTimezone=GMT%2b8&useSSL=false",
                "root",
                "123456",
                1L,
                10L,
                1000L,
                1000L,
                5000L,
                "0",
                DataSourceFromEn.CONFIG);
        DataSource dataSource = DataSourceFactory.buildDataSource(dsConfig);
        Jdbc jdbc = new EasyJdbc(dataSource);
//        List<EasyRecord> query = jdbc.query("select * from ssh_server", 1, 10);
//        System.err.println(JSON.toJSONString(query));


//        SysUser sysUser = new SysUser();
//        sysUser.setPkUser(SnowPkGenerator.generateSnow());
//        sysUser.setUserName("test3");
//        sysUser.setPassword("test3");
//        sysUser.setDtCreated(LocalDateTime.now());
//        sysUser.setDtModified(LocalDateTime.now());
//
//        jdbc.addIgnoreNull(sysUser);
//
//        sysUser = new SysUser();
//        sysUser.setPkUser(1914918557028847616L);
//        sysUser.setUserName("test1");
//        sysUser.setPassword("test1");
//        sysUser.setDtModified(LocalDateTime.now());
//        jdbc.updateIgnoreNull(sysUser);

        List<ZenSysUser> sysUsers = jdbc.queryBeansByFile(ZenSysUser.class, "test", "query", new EasyParams(), new EasyVars());
        System.err.println(JSON.toJSONString(sysUsers));
    }


    @Test
    public void test2() throws IOException {
//        Jdbcs.propertiesJdbc();
    }

}
