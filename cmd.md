docker-compose build 

导出镜像
docker save -o zen-ops-latest.tar zen-ops:latest

导入镜像
docker load -i zen-ops-latest.tar


启动images
docker-compose up -d
