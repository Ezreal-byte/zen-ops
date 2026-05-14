# Zen-Ops Docker 部署指南

## 📦 目录结构

```
zen-ops/
├── docker/
│   ├── config/          # 配置文件目录（映射到容器外）
│   │   ├── application.yml    # 应用配置
│   │   └── logback.xml        # 日志配置
│   └── logs/            # 日志目录（映射到容器外）
├── Dockerfile           # Docker 镜像构建文件
├── docker-compose.yml   # Docker Compose 编排文件
├── .dockerignore        # Docker 构建忽略文件
├── docker-ops.bat       # Windows 快捷脚本
└── docker-ops.sh        # Linux/Mac 快捷脚本
```

## 🚀 快速开始

### 1. 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- Maven 3.6+
- JDK 8+

### 2. 一键构建和启动

#### Windows 系统

```powershell
# 构建镜像
docker-ops.bat build

# 启动容器
docker-ops.bat start
```

#### Linux/Mac 系统

```bash
# 添加执行权限
chmod +x docker-ops.sh

# 构建镜像
./docker-ops.sh build

# 启动容器
./docker-ops.sh start
```

### 3. 访问应用

启动成功后，访问：http://localhost:9998/platform

## 📝 详细说明

### 配置文件说明

首次构建时，脚本会自动将默认配置文件复制到 `docker/config/` 目录：

- `docker/config/application.yml` - 应用配置（数据库连接、端口等）
- `docker/config/logback.xml` - 日志配置

**重要**：启动前请修改 `docker/config/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-host:3306/zen_ops?useUnicode=true&characterEncoding=utf8&autoReconnect=true&serverTimezone=GMT%2b8&useSSL=false
    username: your-username
    password: your-password
```

### 目录映射

| 宿主机路径 | 容器路径 | 说明 |
|-----------|---------|------|
| `./docker/config` | `/app/config` | 配置文件目录 |
| `./docker/logs` | `/app/logs` | 日志文件目录 |

### 常用命令

#### 使用快捷脚本

```bash
# 构建镜像
docker-ops.bat build        # Windows
./docker-ops.sh build       # Linux/Mac

# 启动容器
docker-ops.bat start
./docker-ops.sh start

# 停止容器
docker-ops.bat stop
./docker-ops.sh stop

# 重启容器
docker-ops.bat restart
./docker-ops.sh restart

# 查看日志
docker-ops.bat logs
./docker-ops.sh logs

# 查看状态
docker-ops.bat status
./docker-ops.sh status

# 清理资源
docker-ops.bat clean
./docker-ops.sh clean
```

#### 使用 Docker Compose 原生命令

```bash
# 构建镜像
docker-compose build

# 启动容器
docker-compose up -d

# 停止容器
docker-compose down

# 查看日志
docker-compose logs -f

# 查看状态
docker-compose ps

# 重启容器
docker-compose restart
```

## 🔧 高级配置

### 修改 JVM 参数

编辑 `docker-compose.yml` 文件：

```yaml
environment:
  - JAVA_OPTS=-Xms512m -Xmx2048m -Djava.security.egd=file:/dev/./urandom
```

### 修改端口映射

编辑 `docker-compose.yml` 文件：

```yaml
ports:
  - "8080:9998"  # 将宿主机的 8080 端口映射到容器的 9998 端口
```

### 自定义日志配置

1. 修改 `docker/config/logback.xml`
2. 重启容器使其生效：

```bash
docker-ops.bat restart
```

### 查看容器内的文件

```bash
# 进入容器
docker exec -it zen-ops-server /bin/sh

# 查看文件
ls -la /app/
ls -la /app/config/
ls -la /app/logs/
```

## 🐛 故障排查

### 1. 容器启动失败

查看日志：
```bash
docker-compose logs -f
```

### 2. 数据库连接失败

检查 `docker/config/application.yml` 中的数据库配置是否正确。

### 3. 端口被占用

修改 `docker-compose.yml` 中的端口映射：
```yaml
ports:
  - "9999:9998"  # 改为其他端口
```

### 4. 配置文件未生效

确保配置文件在 `docker/config/` 目录下，并且容器已重启。

## 📊 监控和维护

### 查看容器资源使用

```bash
docker stats zen-ops-server
```

### 清理无用镜像

```bash
docker image prune -a
```

### 备份数据和配置

```bash
# 备份配置文件
tar -czf config-backup-$(date +%Y%m%d).tar.gz docker/config/

# 备份日志文件
tar -czf logs-backup-$(date +%Y%m%d).tar.gz docker/logs/
```

## 🔄 更新应用

```bash
# 1. 停止容器
docker-ops.bat stop

# 2. 重新构建镜像
docker-ops.bat build

# 3. 启动容器
docker-ops.bat start
```

## 📞 支持

如有问题，请查看：
- 应用日志：`docker/logs/`
- 容器日志：`docker-compose logs -f`
