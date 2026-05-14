# Docker 镜像加速配置

## 问题
下载 Docker 镜像时遇到 502 错误或速度很慢。

## 解决方案：配置国内镜像加速器

### 方法一：通过 Docker Desktop 界面配置（推荐）

1. 右键点击任务栏的 Docker 图标
2. 选择 **Settings** (设置)
3. 左侧选择 **Docker Engine**
4. 在 JSON 配置中添加 `"registry-mirrors"`：

```json
{
  "builder": {
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://docker.1panel.live",
    "https://hub.rat.dev"
  ]
}
```

5. 点击 **Apply & restart** 保存并重启 Docker

### 方法二：使用阿里云镜像加速器

1. 访问 https://cr.console.aliyun.com/
2. 登录后，点击左侧 **镜像加速器**
3. 复制你的专属加速地址
4. 按照方法一的步骤，将地址添加到 `registry-mirrors` 中

### 验证配置

```powershell
docker info | Select-String -Pattern "Registry Mirrors" -Context 0,5
```

## 清理缓存重试

配置完成后，清理缓存并重新构建：

```powershell
# 清理 Docker 缓存
docker system prune -a

# 重新构建
docker-ops.bat build
```

## 备用方案：手动下载镜像

如果自动下载仍然失败，可以手动拉取基础镜像：

```powershell
# 手动拉取基础镜像
docker pull eclipse-temurin:8-jre

# 然后重新构建
docker-ops.bat build
```
