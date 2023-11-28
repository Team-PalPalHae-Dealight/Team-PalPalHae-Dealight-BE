#!/bin/bash
cd /home/ubuntu/app

# REST DOCS 문서 파일 이동
source_dir="/home/ubuntu/app"
target_dir="/home/ubuntu/docs"

sudo find "$source_dir" -name "*.html" -exec mv {} "$target_dir" \;
echo "모든 .html 파일을 $target_dir로 옮겼습니다."

# docker-compose.infra를 실행하여 꺼진 컨테이너가 있으면 다시 실행
echo "docker-compose.infra를 실행합니다..."
sudo docker compose -f docker-compose.infra.yml up -d
