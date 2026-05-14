# Zen-Ops 快速启动指南

## 🎯 一次编译，多数据库支持

本项目采用**一次编译，配置切换**的设计理念：
- ✅ 只需编译一次 JAR/Docker 镜像
- ✅ 通过配置文件切换数据库
- ✅ SQLite 和 MySQL 使用相同的代码和镜像
- ✅ 唯一区别：SQLite 需要映射数据文件目录

### 支持的数据库
- ✅ **MySQL**（生产推荐）
- ✅ **SQLite**（轻量级/开发测试）
- ✅ PostgreSQL
- ✅ Oracle
- ✅ SQL Server

---

## 🚀 快速启动

### 方式一：直接运行 JAR

#### 1. 编译项目（只需一次）

```bash
mvn clean package -DskipTests
```

#### 2. 选择数据库启动

**使用 MySQL：**
```bash
java -jar zen-ops-server.jar --spring.profiles.active=mysql
```

**使用 SQLite：**
```bash
# 先初始化数据库（首次）
sqlite3 zen_ops.db < init-sqlite.sql

# 启动应用
java -jar zen-ops-server.jar --spring.profiles.active=sqlite
```

---

### 方式二：Docker 部署

#### 1. 构建镜像（只需一次）

```bash
docker-compose build
```

#### 2. 选择数据库启动

**使用 MySQL（默认）：**
```bash
docker-compose up -d
```

**使用 SQLite：**
```bash
# 创建数据目录
mkdir -p docker/data

# 初始化数据库（首次）
sqlite3 docker/data/zen_ops.db < init-sqlite.sql

# 启动（使用 SQLite profile）
docker-compose up -d zen-ops-sqlite
```

---

## 📋 Docker Compose 配置对比

SQLite 和 MySQL 的配置**几乎相同**，唯一区别是数据目录映射：

### MySQL 配置
```yaml
services:
  zen-ops:
    image: zen-ops:latest
    ports:
      - "9998:9998"
    volumes:
      - ./docker/config:/app/config  # 配置文件
      - ./docker/logs:/app/logs      # 日志文件
    environment:
      - SPRING_PROFILES_ACTIVE=mysql
      # MySQL 数据源配置
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/zen_ops
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=123456
```

### SQLite 配置
```yaml
services:
  zen-ops-sqlite:
    image: zen-ops:latest
    ports:
      - "9998:9998"
    volumes:
      - ./docker/config:/app/config  # 配置文件
      - ./docker/logs:/app/logs      # 日志文件
      - ./docker/data:/data          # ⭐ SQLite 数据文件（唯一区别）
    environment:
      - SPRING_PROFILES_ACTIVE=sqlite
      # SQLite 数据源配置
      - SPRING_DATASOURCE_URL=jdbc:sqlite:/data/zen_ops.db
      - SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.sqlite.JDBC
```

**对比总结：**
- ✅ 相同的镜像
- ✅ 相同的配置文件结构
- ✅ 相同的日志配置
- ⭐ **唯一区别**：SQLite 多了 `- ./docker/data:/data` 数据目录映射

---

## 🔧 Nginx 反向代理配置

如果你的应用需要通过 Nginx 代理访问，请使用以下配置：

### 基础配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 代理 Zen-Ops 应用
    location ^~ /platform/ {
        proxy_pass http://xx.xx.xx.xx:9998/platform/;
        
        # 基础代理头
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 完整配置（支持 WebSocket）

> ⚠️ **重要**：SSH 终端功能依赖 WebSocket，必须添加以下配置！

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 代理 Zen-Ops 应用
    location ^~ /platform/ {
        proxy_pass http://xx.xx.xx.xx:9998/platform/;
        
        # ===== 必须：WebSocket 支持（5 行） =====
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 86400s;
        proxy_send_timeout 86400s;
        
        # 基础代理头
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 配置说明

| 配置项 | 说明 | 必须性 |
|--------|------|--------|
| `location ^~ /platform/` | 匹配 /platform/ 路径，^~ 表示优先匹配 | ✅ 必须 |
| `proxy_pass` | 后端服务地址，注意末尾的 `/` | ✅ 必须 |
| `proxy_http_version 1.1` | 升级到 HTTP/1.1 支持 WebSocket | ✅ 必须（SSH） |
| `proxy_set_header Upgrade` | 传递 WebSocket 升级请求 | ✅ 必须（SSH） |
| `proxy_set_header Connection "upgrade"` | 设置连接升级为 WebSocket | ✅ 必须（SSH） |
| `proxy_read_timeout 86400s` | SSH 会话超时时间（24小时） | ✅ 必须（SSH） |
| `proxy_send_timeout 86400s` | 发送超时时间（24小时） | ✅ 必须（SSH） |
| `proxy_set_header Host` | 传递原始 Host 头 | 推荐 |
| `proxy_set_header X-Real-IP` | 传递真实 IP | 推荐 |
| `proxy_set_header X-Forwarded-For` | 传递转发链 IP | 推荐 |

### 完整示例（含 HTTPS）

```nginx
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    
    location ^~ /platform/ {
        proxy_pass http://127.0.0.1:9998/platform/;
        
        # WebSocket 支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 86400s;
        proxy_send_timeout 86400s;
        
        # 代理头
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 测试 Nginx 配置

```bash
# 测试配置语法
nginx -t

# 重新加载配置
nginx -s reload

# 查看 Nginx 日志
tail -f /var/log/nginx/error.log
```

---

## 📊 数据库选择建议

| 场景 | 推荐数据库 | 原因 |
|------|-----------|------|
| 本地开发 | SQLite | 零配置、快速启动 |
| 单元测试 | SQLite | 轻量、易清理 |
| 集成测试 | MySQL | 与生产一致 |
| 生产环境 | MySQL/PostgreSQL | 高性能、高可用 |
| 边缘计算 | SQLite | 资源占用极低 |
| 演示环境 | SQLite | 一键启动 |

---

## ⚡ 常用命令

```bash
# 编译
mvn clean package -DskipTests

# Docker 构建
docker-compose build

# Docker 启动（MySQL）
docker-compose up -d

# Docker 启动（SQLite）
docker-compose up -d zen-ops-sqlite

# 查看日志
docker-compose logs -f

# 停止
docker-compose down

# 备份 SQLite 数据
cp docker/data/zen_ops.db docker/data/zen_ops.db.backup
```

---

## 🔍 验证

### 1. 访问应用

http://localhost:9998/platform

### 2. 查看启动日志

```bash
# Docker 方式
docker-compose logs -f

# JAR 方式
tail -f logs/log.log
```

确认看到：
```
========================================
  Database: MySQL 或 SQLite
  Dialect: mysql 或 sqlite
  URL: jdbc:...
========================================
```

### 3. 测试 SSH 终端（如果通过 Nginx）

1. 登录系统
2. 进入 SSH 终端页面
3. 连接服务器
4. 如果终端正常工作，说明 WebSocket 配置正确

---

## 📁 目录结构

```
zen-ops/
├── docker/
│   ├── config/          # 外部配置文件（可选）
│   ├── logs/            # 日志文件
│   └── data/            # SQLite 数据文件（仅 SQLite 需要）
│       └── zen_ops.db
├── docker-compose.yml   # Docker 编排文件
├── Dockerfile           # Docker 镜像构建文件
├── init-sqlite.sql      # SQLite 初始化脚本
└── zen-ops-server.jar   # 编译后的 JAR
```

---

## 💡 核心优势

- ✅ **一次编译**：只需构建一次 JAR/Docker 镜像
- ✅ **配置切换**：通过 profile 切换数据库
- ✅ **自动识别**：PageHelper 自动识别数据库方言
- ✅ **简化部署**：SQLite 和 MySQL 使用相同镜像
- ✅ **唯一区别**：SQLite 只需多映射一个数据目录

---

## 📚 更多文档

- [Docker 部署指南](DOCKER.md)

---

**祝你使用愉快！** 🎉
