package com.ops.zen;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ops.zen.en.DsConnTypeEn;
import com.ops.zen.en.DbTypeEn;
import com.ops.zen.entity.ZenDbDs;
import com.ops.zen.jdbc.Jdbc;
import com.ops.zen.mapper.ZenSysUserMapper;
import com.ops.zen.meta.MetaUtils;
import com.ops.zen.meta.TableInfo;
import com.ops.zen.utils.Jdbcs;
import com.ops.zen.utils.JsonUtils;
import com.ops.zen.utils.pk.SnowPkGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.*;

/**
 * @author xyn
 * @date 2025/4/11 18:53
 * @description
 **/
@SpringBootTest
public class SpringBootTest2 {

    @Autowired
    private ZenSysUserMapper sysUserMapper;


    @Test
    public void test2() {
        ZenDbDs ds = new ZenDbDs();
        //输出json字符串 包括空值

        System.err.println(JSON.toJSONString(ds, SerializerFeature.WriteMapNullValue));
    }

    @Test
    public void insertTestDataSource() {
        ZenDbDs ds = new ZenDbDs();
        ds.setPkDs(SnowPkGenerator.generateSnow());
        ds.setDbType(DbTypeEn.MYSQL);
        ds.setConnType(DsConnTypeEn.URL);
        ds.setName("localhost@mysql@url");
        ds.setUrl("jdbc:mysql://localhost:3306/pixel_grid?useUnicode=true&characterEncoding=utf8&autoReconnect=true&serverTimezone=GMT%2b8&useSSL=false");
        ds.setUserName("root");
        ds.setUserPwd("123456");
        ds.setDriver("com.mysql.cj.jdbc.Driver");
        ds.setConnMax(10D);
        ds.setConnMin(1D);
        ds.setDes("localhost URL方式连接");
//        Jdbcs.propertiesJdbc().addIgnoreNull(ds);

        ds.setPkDs(SnowPkGenerator.generateSnow());
        ds.setConnType(DsConnTypeEn.HOST);
        ds.setName("localhost@mysql@host");
        ds.setUrl("");
        ds.setHost("localhost");
        ds.setPort("3306");
        ds.setDbSchema("pixel_grid");
        ds.setDes("localhost HOST方式连接");
//        Jdbcs.propertiesJdbc().addIgnoreNull(ds);
    }

    @Test
    public void test3() {
        Jdbc urlJdbc = Jdbcs.getJdbc("1914949653363822592");
        System.err.println("URL_JDBC ===> " +
                JsonUtils.toJSONString(urlJdbc.query("select * from sys_user", 1, 10))
        );
        Jdbc hostJdbc = Jdbcs.getJdbc("1914950382367412225");
        System.err.println("HOST_JDBC ===> " +
                JsonUtils.toJSONString(hostJdbc.query("select * from sys_user", 1, 10))
        );
    }

    @Test
    public void test4() throws SQLException {
//        MySqlHelper.
        Jdbc jdbc = Jdbcs.getJdbc("1914949653363822592");
        TableInfo tableInfo = MetaUtils.tableInfo(jdbc, "pixel_grid", "ssh_server");

        System.err.println(JsonUtils.toJSONString(tableInfo));
//        Connection connection = jdbc.getDataSource().getConnection();
//        DatabaseMetaData metaData = connection.getMetaData();
//        ResultSet resultSet = metaData.getColumns(null, "pixel_grid", "sys_user", null);
//        while (resultSet.next()) {
//            System.err.println(resultSet.getString("COLUMN_NAME"));
//        }
    }
}
