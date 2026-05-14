@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 设置 Maven 路径（如果未配置环境变量）
set MAVEN_HOME=D:\apache-maven-3.6.3
set PATH=%MAVEN_HOME%\bin;%PATH%

echo =========================================
echo   Zen-Ops Docker 构建脚本
echo =========================================
echo.

REM 检查 Docker 是否安装
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] Docker 未安装，请先安装 Docker
    pause
    exit /b 1
)

where docker-compose >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] Docker Compose 未安装，请先安装 Docker Compose
    pause
    exit /b 1
)

REM 创建必要的目录
echo [信息] 创建配置和日志目录...
if not exist "docker\config" mkdir docker\config
if not exist "docker\logs" mkdir docker\logs

REM 检查配置文件是否存在
if not exist "docker\config\application.yml" (
    echo [信息] 复制默认配置文件到 docker\config\...
    copy "zen-ops-server\src\main\resources\application.yml" "docker\config\application.yml"
    echo [成功] 请根据需要修改 docker\config\application.yml 配置文件
)

if not exist "docker\config\logback.xml" (
    echo [信息] 复制默认日志配置到 docker\config\...
    copy "zen-ops-server\src\main\resources\logback.xml" "docker\config\logback.xml"
)

REM 解析参数
set ACTION=%1
if "%ACTION%"=="" set ACTION=build

if "%ACTION%"=="build" (
    echo [信息] 开始构建 Docker 镜像...
    
    REM Maven 打包
    echo [信息] 执行 Maven 打包...
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo [错误] Maven 打包失败
        pause
        exit /b 1
    )
    
    REM 构建 Docker 镜像
    echo [信息] 构建 Docker 镜像...
    docker-compose build
    if %errorlevel% neq 0 (
        echo [错误] Docker 镜像构建失败
        pause
        exit /b 1
    )
    
    echo.
    echo [成功] Docker 镜像构建完成！
    echo [提示] 运行命令: docker-ops.bat start
    
) else if "%ACTION%"=="start" (
    echo [信息] 启动 Zen-Ops 容器...
    docker-compose up -d
    echo [成功] 容器已启动！
    echo [提示] 查看日志: docker-compose logs -f
    echo [提示] 访问地址: http://localhost:9998/platform
    
) else if "%ACTION%"=="stop" (
    echo [信息] 停止 Zen-Ops 容器...
    docker-compose down
    echo [成功] 容器已停止
    
) else if "%ACTION%"=="restart" (
    echo [信息] 重启 Zen-Ops 容器...
    docker-compose restart
    echo [成功] 容器已重启
    
) else if "%ACTION%"=="logs" (
    echo [信息] 查看容器日志...
    docker-compose logs -f
    
) else if "%ACTION%"=="status" (
    echo [信息] 查看容器状态...
    docker-compose ps
    
) else if "%ACTION%"=="clean" (
    echo [信息] 清理 Docker 资源...
    docker-compose down
    docker rmi zen-ops:latest 2>nul
    echo [成功] 清理完成
    
) else (
    echo [错误] 用法: %0 {build^|start^|stop^|restart^|logs^|status^|clean}
    echo.
    echo 命令说明:
    echo   build    - 构建 Docker 镜像
    echo   start    - 启动容器
    echo   stop     - 停止容器
    echo   restart  - 重启容器
    echo   logs     - 查看日志
    echo   status   - 查看状态
    echo   clean    - 清理资源
    pause
    exit /b 1
)

echo.
echo =========================================
echo   操作完成！
echo =========================================
pause
