#!/bin/bash

# ubuntu 프로세스 매니저 버그 해결을 위해 추가
sudo aa-remove-unknown

cd /home/ubuntu/app
DOCKER_APP_NAME=dealight

# 실행중인 blue가 있는지
EXIST_BLUE=$(docker ps | grep ${DOCKER_APP_NAME}-blue-container)

# green이 실행중이면 blue up
if [ -z "$EXIST_BLUE" ]; then
  echo "Green 컨테이너가 실행 중이므로 Blue 컨테이너를 실행(UP)합니다..."
  sudo docker compose -f docker-compose.blue.yml up -d

  sleep 30

  sudo docker compose -f docker-compose.green.yml down
  sudo docker image prune -af # 사용하지 않는 이미지 삭제

# blue가 실행중이면 green up
else
  echo "Blue 컨테이너가 실행 중이므로 Green 컨테이너를 실행(UP)합니다..."
  sudo docker compose -f docker-compose.green.yml up -d

  sleep 30

  sudo docker compose -f docker-compose.blue.yml down
  sudo docker image prune -af
fi
