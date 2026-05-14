/*
 Zen-Ops SQLite 初始化脚本

 Source Server Type    : SQLite
 Target Server Type    : SQLite
 Target Server Version : 3.x
 File Encoding         : 65001 (UTF-8)

 Date: 06/05/2026

 注意：SQLite 不支持 COMMENT ON 语法，注释以 SQL 注释形式保留
*/

-- 开启外键约束
PRAGMA foreign_keys = ON;

-- ----------------------------
-- Table structure for zen_db_ds (数据源)
-- ----------------------------
DROP TABLE IF EXISTS zen_db_ds;
CREATE TABLE zen_db_ds (
  PK_DS INTEGER NOT NULL PRIMARY KEY, -- 主键
  DB_TYPE TEXT DEFAULT NULL, -- 数据库类型
  CONN_TYPE TEXT DEFAULT NULL, -- 连接方式 1主机2URL
  NAME TEXT DEFAULT NULL, -- 名称
  HOST TEXT DEFAULT NULL, -- IP地址
  PORT TEXT DEFAULT NULL, -- 端口号
  DB_SCHEMA TEXT DEFAULT NULL, -- 数据库
  URL TEXT DEFAULT NULL, -- 链接地址
  USER_NAME TEXT DEFAULT NULL, -- 用户名
  USER_PWD TEXT DEFAULT NULL, -- 密码
  DRIVER TEXT DEFAULT NULL, -- 驱动
  CONN_MAX INTEGER DEFAULT NULL, -- 最大连接数
  CONN_MIN INTEGER DEFAULT NULL, -- 最小连接数
  DES TEXT DEFAULT NULL, -- 描述
  IS_SYS INTEGER DEFAULT 0, -- 是否系统数据
  IS_LOCK INTEGER DEFAULT 0, -- 是否锁定数据
  PK_CREATEDBY INTEGER DEFAULT NULL, -- 创建人
  DT_CREATED DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  PK_MODIFIEDBY INTEGER DEFAULT NULL, -- 修改人
  DT_MODIFIED DATETIME DEFAULT NULL, -- 修改时间
  DS INTEGER DEFAULT 0, -- 删除标志
  VERSION INTEGER DEFAULT NULL -- 版本
);

-- ----------------------------
-- Table structure for zen_db_exec_log (SQL执行审计日志)
-- ----------------------------
DROP TABLE IF EXISTS zen_db_exec_log;
CREATE TABLE zen_db_exec_log (
  PK_LOG INTEGER NOT NULL PRIMARY KEY, -- 主键
  PK_DS INTEGER DEFAULT NULL, -- 数据源ID
  DB_SCHEMA TEXT DEFAULT NULL, -- 数据库名
  SQL_TEXT TEXT DEFAULT NULL, -- 执行的SQL内容
  SQL_TYPE TEXT DEFAULT NULL, -- SQL类型 DDL/DML
  EXEC_STATUS TEXT DEFAULT NULL, -- 执行状态 SUCCESS/FAIL
  EXEC_TIME_MS INTEGER DEFAULT NULL, -- 执行耗时毫秒
  ERROR_MSG TEXT DEFAULT NULL, -- 错误信息
  AFFECTED_ROWS INTEGER DEFAULT NULL, -- 影响行数
  PK_CREATEDBY INTEGER DEFAULT NULL, -- 执行人
  DT_CREATED DATETIME DEFAULT CURRENT_TIMESTAMP -- 执行时间
);

-- ----------------------------
-- Table structure for zen_fso_ds (对象存储数据源)
-- ----------------------------
DROP TABLE IF EXISTS zen_fso_ds;
CREATE TABLE zen_fso_ds (
  PK_FSO_DS INTEGER NOT NULL PRIMARY KEY, -- 主键
  NAME TEXT DEFAULT NULL, -- 数据源名称
  TYPE TEXT DEFAULT NULL, -- 类型(MINIO/ALIYUN_OSS)
  CLOB_CONFIG TEXT DEFAULT NULL, -- 配置信息JSON
  IS_SYS INTEGER DEFAULT 0, -- 是否系统数据
  IS_LOCK INTEGER DEFAULT 0, -- 是否锁定数据
  PK_CREATEDBY INTEGER DEFAULT NULL, -- 创建人
  DT_CREATED DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  PK_MODIFIEDBY INTEGER DEFAULT NULL, -- 修改人
  DT_MODIFIED DATETIME DEFAULT NULL, -- 修改时间
  DS INTEGER DEFAULT 0, -- 删除标志
  VERSION INTEGER DEFAULT NULL -- 版本
);

-- ----------------------------
-- Table structure for zen_redis_ds (Redis数据源)
-- ----------------------------
DROP TABLE IF EXISTS zen_redis_ds;
CREATE TABLE zen_redis_ds (
  PK_REDIS_DS INTEGER NOT NULL PRIMARY KEY, -- 主键
  NAME TEXT DEFAULT NULL, -- 数据源名称
  HOST TEXT DEFAULT NULL, -- 主机地址
  PORT TEXT DEFAULT NULL, -- 端口号
  PASSWORD TEXT DEFAULT NULL, -- 密码
  DATABASE_NUM INTEGER DEFAULT 16, -- 数据库数量(0-16)
  TIMEOUT INTEGER DEFAULT 3000, -- 连接超时时间(毫秒)
  DES TEXT DEFAULT NULL, -- 描述
  IS_SYS INTEGER DEFAULT 0, -- 是否系统数据
  IS_LOCK INTEGER DEFAULT 0, -- 是否锁定数据
  PK_CREATEDBY INTEGER DEFAULT NULL, -- 创建人
  DT_CREATED DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  PK_MODIFIEDBY INTEGER DEFAULT NULL, -- 修改人
  DT_MODIFIED DATETIME DEFAULT NULL, -- 修改时间
  DS INTEGER DEFAULT 0, -- 删除标志
  VERSION INTEGER DEFAULT NULL -- 版本
);

-- ----------------------------
-- Table structure for zen_schedule (定时任务)
-- ----------------------------
DROP TABLE IF EXISTS zen_schedule;
CREATE TABLE zen_schedule (
  pk_schedule INTEGER NOT NULL PRIMARY KEY, -- 任务主键
  pk_schedule_group INTEGER DEFAULT NULL, -- 分组主键
  name TEXT DEFAULT NULL, -- 定时任务名称
  job_class TEXT DEFAULT NULL, -- 任务类
  trigger_type TEXT DEFAULT NULL, -- 触发器类型 simple简单触发器固定间隔 cron日程触发器
  trigger_expr TEXT DEFAULT NULL, -- 表达式, CRON或秒
  clob_cfg TEXT DEFAULT NULL, -- 配置信息
  principal TEXT DEFAULT NULL, -- 负责人
  is_active INTEGER DEFAULT NULL, -- 是否激活1激活0未激活
  clob_run_time TEXT DEFAULT NULL, -- 运行时信息
  last_run_status INTEGER DEFAULT NULL, -- 上次运行状态1成功0失败
  des TEXT DEFAULT NULL, -- 描述
  pk_createdby INTEGER DEFAULT NULL, -- 创建人
  dt_created DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  pk_modifiedby INTEGER DEFAULT NULL, -- 修改人
  dt_modified DATETIME DEFAULT NULL, -- 修改时间
  ds INTEGER DEFAULT 0, -- 删除标志
  version INTEGER DEFAULT NULL -- 版本
);

-- ----------------------------
-- Table structure for zen_schedule_group (任务分组)
-- ----------------------------
DROP TABLE IF EXISTS zen_schedule_group;
CREATE TABLE zen_schedule_group (
  pk_schedule_group INTEGER NOT NULL PRIMARY KEY, -- 主键
  name TEXT DEFAULT NULL, -- 分组名称
  pk_createdby INTEGER DEFAULT NULL, -- 创建人
  dt_created DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  pk_modifiedby INTEGER DEFAULT NULL, -- 修改人
  dt_modified DATETIME DEFAULT NULL, -- 修改时间
  ds INTEGER DEFAULT 0, -- 删除标志
  version INTEGER DEFAULT NULL -- 版本
);

-- ----------------------------
-- Table structure for zen_schedule_log (定时任务日志)
-- ----------------------------
DROP TABLE IF EXISTS zen_schedule_log;
CREATE TABLE zen_schedule_log (
  pk_schedule_log INTEGER NOT NULL PRIMARY KEY, -- 主键
  pk_schedule INTEGER DEFAULT NULL, -- 任务主键
  dt_run DATETIME DEFAULT NULL, -- 运行时间
  status_run INTEGER DEFAULT NULL, -- 运行状态 1成功0失败 2执行中
  result_run TEXT DEFAULT NULL, -- 运行结果
  elapsed_time TEXT DEFAULT NULL, -- 耗时 ms
  clob_log TEXT DEFAULT NULL, -- 运行日志
  run_node TEXT DEFAULT NULL, -- 运行节点标志
  run_cfg TEXT DEFAULT NULL, -- 运行时配置
  is_manual INTEGER DEFAULT NULL, -- 是否手动
  pk_createdby INTEGER DEFAULT NULL, -- 创建人
  dt_created DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  pk_modifiedby INTEGER DEFAULT NULL, -- 修改人
  dt_modified DATETIME DEFAULT NULL, -- 修改时间
  ds INTEGER DEFAULT 0, -- 删除标志
  version INTEGER DEFAULT NULL -- 版本
);

-- ----------------------------
-- Table structure for zen_ssh (服务器配置表)
-- ----------------------------
DROP TABLE IF EXISTS zen_ssh;
CREATE TABLE zen_ssh (
  PK_SERVER INTEGER NOT NULL PRIMARY KEY, -- 主键
  NAME TEXT DEFAULT NULL, -- 名称
  DES TEXT DEFAULT NULL, -- 描述
  IP TEXT DEFAULT NULL, -- 服务器IP地址
  PORT_SSH TEXT DEFAULT NULL, -- SSH端口号
  USER_NAME TEXT DEFAULT NULL, -- 用户名
  USER_PWD TEXT DEFAULT NULL, -- 密码
  LOGIN_TP TEXT DEFAULT NULL, -- 登录类型(密码/私钥)
  PRV_KEY TEXT DEFAULT NULL, -- 私钥
  PRV_KEY_PASSWD TEXT DEFAULT NULL, -- 私钥密码
  INIT_PATH TEXT DEFAULT NULL, -- 默认进入目录
  IS_SYS INTEGER DEFAULT 0, -- 是否系统数据
  IS_LOCK INTEGER DEFAULT 0, -- 是否锁定数据
  PK_CREATEDBY INTEGER DEFAULT NULL, -- 创建人
  DT_CREATED DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  PK_MODIFIEDBY INTEGER DEFAULT NULL, -- 修改人
  DT_MODIFIED DATETIME DEFAULT NULL, -- 修改时间
  DS INTEGER DEFAULT 0, -- 删除标志
  VERSION INTEGER DEFAULT NULL -- 版本
);

-- ----------------------------
-- Table structure for zen_sys_menu (菜单表)
-- ----------------------------
DROP TABLE IF EXISTS zen_sys_menu;
CREATE TABLE zen_sys_menu (
  PK_MENU INTEGER NOT NULL PRIMARY KEY, -- 主键
  PK_PARENT INTEGER DEFAULT NULL, -- 父主键
  SUB_COUNT INTEGER DEFAULT 0, -- 子菜单数目
  NODE_TYPE TEXT DEFAULT NULL, -- 菜单类型（功能节点、外链等）
  NAME TEXT DEFAULT NULL, -- 菜单名称
  ICON TEXT DEFAULT NULL, -- 菜单图标
  URL TEXT DEFAULT NULL, -- 菜单组件路径/外链地址
  COMPONENT TEXT DEFAULT NULL, -- 前端组件路径
  SORT_ORDER INTEGER DEFAULT NULL, -- 排序
  IS_HIDDEN INTEGER DEFAULT NULL, -- 是否隐藏
  IS_SYS INTEGER DEFAULT 0, -- 是否系统数据
  IS_LOCK INTEGER DEFAULT 0, -- 是否锁定数据
  PK_CREATEDBY INTEGER DEFAULT NULL, -- 创建人
  DT_CREATED DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  PK_MODIFIEDBY INTEGER DEFAULT NULL, -- 修改人
  DT_MODIFIED DATETIME DEFAULT NULL, -- 修改时间
  DS INTEGER DEFAULT 0, -- 删除标志
  VERSION INTEGER DEFAULT NULL -- 版本
);

-- ----------------------------
-- Table structure for zen_sys_menu_role (菜单角色中间表)
-- ----------------------------
DROP TABLE IF EXISTS zen_sys_menu_role;
CREATE TABLE zen_sys_menu_role (
  PK_MENU_ROLE INTEGER NOT NULL PRIMARY KEY, -- 主键
  PK_MENU INTEGER DEFAULT NULL, -- 菜单主键
  PK_ROLE INTEGER DEFAULT NULL, -- 角色主键
  IS_SYS INTEGER DEFAULT 0, -- 是否系统数据
  IS_LOCK INTEGER DEFAULT 0, -- 是否锁定数据
  PK_CREATEDBY INTEGER DEFAULT NULL, -- 创建人
  DT_CREATED DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  PK_MODIFIEDBY INTEGER DEFAULT NULL, -- 修改人
  DT_MODIFIED DATETIME DEFAULT NULL, -- 修改时间
  DS INTEGER DEFAULT 0, -- 删除标志
  VERSION INTEGER DEFAULT NULL -- 版本
);

-- ----------------------------
-- Table structure for zen_sys_role (角色表)
-- ----------------------------
DROP TABLE IF EXISTS zen_sys_role;
CREATE TABLE zen_sys_role (
  PK_ROLE INTEGER NOT NULL PRIMARY KEY, -- 主键
  NAME TEXT DEFAULT NULL, -- 名称
  IS_SYS INTEGER DEFAULT 0, -- 是否系统数据
  IS_LOCK INTEGER DEFAULT 0, -- 是否锁定数据
  PK_CREATEDBY INTEGER DEFAULT NULL, -- 创建人
  DT_CREATED DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  PK_MODIFIEDBY INTEGER DEFAULT NULL, -- 修改人
  DT_MODIFIED DATETIME DEFAULT NULL, -- 修改时间
  DS INTEGER DEFAULT 0, -- 删除标志
  VERSION INTEGER DEFAULT NULL -- 版本
);

-- ----------------------------
-- Table structure for zen_sys_role_user (用户角色中间表)
-- ----------------------------
DROP TABLE IF EXISTS zen_sys_role_user;
CREATE TABLE zen_sys_role_user (
  PK_ROLE_USER INTEGER PRIMARY KEY, -- 主键
  PK_ROLE INTEGER DEFAULT NULL, -- 角色主键
  PK_USER INTEGER DEFAULT NULL, -- 用户主键
  IS_SYS INTEGER DEFAULT 0, -- 是否系统数据
  IS_LOCK INTEGER DEFAULT 0, -- 是否锁定数据
  PK_CREATEDBY INTEGER DEFAULT NULL, -- 创建人
  DT_CREATED DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  PK_MODIFIEDBY INTEGER DEFAULT NULL, -- 修改人
  DT_MODIFIED DATETIME DEFAULT NULL, -- 修改时间
  DS INTEGER DEFAULT 0, -- 删除标志
  VERSION INTEGER DEFAULT NULL -- 版本
);

-- ----------------------------
-- Table structure for zen_sys_user (用户表)
-- ----------------------------
DROP TABLE IF EXISTS zen_sys_user;
CREATE TABLE zen_sys_user (
  PK_USER INTEGER NOT NULL PRIMARY KEY, -- 主键
  USER_NAME TEXT DEFAULT NULL, -- 用户名
  PASSWORD TEXT DEFAULT NULL, -- 密码
  NAME TEXT DEFAULT NULL, -- 姓名
  PHONE TEXT DEFAULT NULL, -- 电话
  EMAIL TEXT DEFAULT NULL, -- 邮箱
  SEX TEXT DEFAULT NULL, -- 性别
  DEPARTMENT TEXT DEFAULT NULL, -- 部门
  BLOB_HEADER BLOB DEFAULT NULL, -- 头像
  IS_SYS INTEGER DEFAULT 0, -- 是否系统数据
  IS_LOCK INTEGER DEFAULT 0, -- 是否锁定数据
  PK_CREATEDBY INTEGER DEFAULT NULL, -- 创建人
  DT_CREATED DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  PK_MODIFIEDBY INTEGER DEFAULT NULL, -- 修改人
  DT_MODIFIED DATETIME DEFAULT NULL, -- 修改时间
  DS INTEGER DEFAULT 0, -- 删除标志
  VERSION INTEGER DEFAULT NULL -- 版本
);

-- SQLite DDL
-- ==========================================
-- SQLite 不支持直接 ADD COLUMN 带默认值的列，需要重建表
-- 但为了简化，我们可以直接添加列

ALTER TABLE `zen_fso_ds` ADD COLUMN `IS_DEFAULT` INTEGER DEFAULT 0;
ALTER TABLE `zen_fso_ds` ADD COLUMN `TAGS` TEXT DEFAULT NULL;

-- SQLite DDL
ALTER TABLE `zen_redis_ds` ADD COLUMN `IS_DEFAULT` INTEGER DEFAULT 0;
ALTER TABLE `zen_redis_ds` ADD COLUMN `TAGS` TEXT DEFAULT NULL;

-- SQLite DDL (SQLite不支持ADD COLUMN AFTER，只能追加到末尾)
ALTER TABLE zen_db_exec_log ADD COLUMN DML_TYPE TEXT;
ALTER TABLE zen_db_exec_log ADD COLUMN SINGLE_TABLE_QUERY INTEGER;
ALTER TABLE zen_db_exec_log ADD COLUMN PK_COLUMN TEXT;
ALTER TABLE zen_db_exec_log ADD COLUMN QUERY_TABLE TEXT;
ALTER TABLE zen_db_exec_log ADD COLUMN QUERY_SCHEMA TEXT;


-- SSH服务器表添加收藏时间和标签字段 (SQLite版本)
-- 执行时间：2026-05-06

-- 添加收藏时间字段
ALTER TABLE zen_ssh
    ADD COLUMN DT_FAVORITE DATETIME DEFAULT NULL; -- 收藏时间

-- 添加标签字段
ALTER TABLE zen_ssh
    ADD COLUMN TAGS VARCHAR(500) DEFAULT NULL; -- 标签(逗号分隔，如: 生产:primary,测试:warning)

-- 注意：SQLite不支持在ALTER TABLE后直接添加索引到已存在的列
-- 如果需要优化查询性能，可以创建索引：
-- CREATE INDEX idx_favorite_created ON zen_ssh(DT_FAVORITE DESC, DT_CREATED DESC);
-- SQL窗口数据源表添加收藏时间和标签字段 (SQLite版本)
-- 执行时间：2026-05-10

-- 添加收藏时间字段
ALTER TABLE zen_db_ds
    ADD COLUMN DT_FAVORITE DATETIME DEFAULT NULL; -- 收藏时间

-- 添加标签字段
ALTER TABLE zen_db_ds
    ADD COLUMN TAGS VARCHAR(500) DEFAULT NULL; -- 标签(逗号分隔，如: 生产:primary,测试:warning)

-- 注意：SQLite不支持在ALTER TABLE后直接添加索引到已存在的列
-- 如果需要优化查询性能，可以创建索引：
-- CREATE INDEX idx_ds_favorite_created ON zen_db_ds(DT_FAVORITE DESC, DT_CREATED DESC);
delete from zen_db_ds where ds = 1;
delete from zen_fso_ds where ds = 1;
delete from zen_redis_ds where ds = 1;
delete from zen_schedule where ds = 1;
delete from zen_schedule_group where ds = 1;
delete from zen_schedule_log where ds = 1;
delete from zen_ssh where ds = 1;
delete from zen_sys_menu where ds = 1;
delete from zen_sys_menu_role where ds = 1;
delete from zen_sys_role where ds = 1;
delete from zen_sys_role_user where ds = 1;
delete from zen_sys_user where ds = 1;
