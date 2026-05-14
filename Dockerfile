# 使用 Eclipse Temurin OpenJDK 8 作为基础镜像（更稳定）
FROM eclipse-temurin:8-jre

# 设置维护者信息
LABEL maintainer="zen-ops"

# 设置工作目录
WORKDIR /app

# 创建日志目录和配置目录
RUN mkdir -p /app/logs /app/config

# 复制 jar 包到容器中
COPY zen-ops-server/target/zen-ops-server-*.jar /app/zen-ops-server.jar

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 暴露端口
EXPOSE 9998

# 设置 JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"

# 启动命令
# 使用外部挂载的配置文件，如果不存在则使用 jar 包内的默认配置
ENTRYPOINT java ${JAVA_OPTS} \
  -jar /app/zen-ops-server.jar \
  --spring.config.additional-location=optional:file:/app/config/
