#!/bin/bash
cd /home/ubuntu/app

# docker-compose.infra를 실행하여 꺼진 컨테이너가 있으면 다시 실행
echo "docker-compose.infra를 실행합니다..."
sudo docker compose -f docker-compose.infra.yml up -d
