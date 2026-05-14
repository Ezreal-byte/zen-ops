#!/bin/bash

# Zen-Ops Docker 构建和运行脚本

set -e

echo "========================================="
echo "  Zen-Ops Docker 构建脚本"
echo "========================================="

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker 未安装，请先安装 Docker${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}错误: Docker Compose 未安装，请先安装 Docker Compose${NC}"
    exit 1
fi

# 创建必要的目录
echo -e "${YELLOW}创建配置和日志目录...${NC}"
mkdir -p docker/config
mkdir -p docker/logs

# 检查配置文件是否存在
if [ ! -f "docker/config/application.yml" ]; then
    echo -e "${YELLOW}复制默认配置文件到 docker/config/...${NC}"
    cp zen-ops-server/src/main/resources/application.yml docker/config/application.yml
    echo -e "${GREEN}✓ 请根据需要修改 docker/config/application.yml 配置文件${NC}"
fi

if [ ! -f "docker/config/logback.xml" ]; then
    echo -e "${YELLOW}复制默认日志配置到 docker/config/...${NC}"
    cp zen-ops-server/src/main/resources/logback.xml docker/config/logback.xml
fi

# 解析参数
ACTION=${1:-"build"}

case $ACTION in
    build)
        echo -e "${YELLOW}开始构建 Docker 镜像...${NC}"
        
        # Maven 打包
        echo -e "${YELLOW}执行 Maven 打包...${NC}"
        mvn clean package -DskipTests
        
        # 构建 Docker 镜像
        echo -e "${YELLOW}构建 Docker 镜像...${NC}"
        docker-compose build
        
        echo -e "${GREEN}✓ Docker 镜像构建完成！${NC}"
        echo -e "${YELLOW}运行命令: ./docker-ops.sh start${NC}"
        ;;
    
    start)
        echo -e "${YELLOW}启动 Zen-Ops 容器...${NC}"
        docker-compose up -d
        echo -e "${GREEN}✓ 容器已启动！${NC}"
        echo -e "${YELLOW}查看日志: docker-compose logs -f${NC}"
        echo -e "${YELLOW}访问地址: http://localhost:9998/platform${NC}"
        ;;
    
    stop)
        echo -e "${YELLOW}停止 Zen-Ops 容器...${NC}"
        docker-compose down
        echo -e "${GREEN}✓ 容器已停止${NC}"
        ;;
    
    restart)
        echo -e "${YELLOW}重启 Zen-Ops 容器...${NC}"
        docker-compose restart
        echo -e "${GREEN}✓ 容器已重启${NC}"
        ;;
    
    logs)
        echo -e "${YELLOW}查看容器日志...${NC}"
        docker-compose logs -f
        ;;
    
    status)
        echo -e "${YELLOW}查看容器状态...${NC}"
        docker-compose ps
        ;;
    
    clean)
        echo -e "${YELLOW}清理 Docker 资源...${NC}"
        docker-compose down
        docker rmi zen-ops:latest 2>/dev/null || true
        echo -e "${GREEN}✓ 清理完成${NC}"
        ;;
    
    *)
        echo -e "${RED}用法: $0 {build|start|stop|restart|logs|status|clean}${NC}"
        echo ""
        echo "命令说明:"
        echo "  build    - 构建 Docker 镜像"
        echo "  start    - 启动容器"
        echo "  stop     - 停止容器"
        echo "  restart  - 重启容器"
        echo "  logs     - 查看日志"
        echo "  status   - 查看状态"
        echo "  clean    - 清理资源"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}  操作完成！${NC}"
echo -e "${GREEN}=========================================${NC}"
