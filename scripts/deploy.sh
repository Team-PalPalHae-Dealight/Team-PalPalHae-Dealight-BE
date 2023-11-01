#!/bin/bash
sudo find /home/ubuntu/app -type f -exec chmod +x {} \;
sudo find /home/ubuntu/app -type d -exec chmod +x {} \;

cd /home/ec2-user/app
sudo docker build -t dealight-api-springboot .
DOCKER_APP_NAME=dealight-api-springboot

# 실행중인 blue가 있는지
EXIST_BLUE=$(docker-compose -p ${DOCKER_APP_NAME}-blue -f docker-compose.blue.yml ps | grep running)

# green이 실행중이면 blue up
if [ -z "$EXIST_BLUE" ]; then
	echo "Green 컨테이너가 실행 중이므로 Blue 컨테이너를 실행(UP)합니다..."
	docker-compose -p ${DOCKER_APP_NAME}-blue -f docker-compose.blue.yml up -d --build

	sleep 30

	docker-compose -p ${DOCKER_APP_NAME}-green -f docker-compose.green.yml down
	docker image prune -af # 사용하지 않는 이미지 삭제

# blue가 실행중이면 green up
else
	echo "Blue 컨테이너가 실행 중이므로 Green 컨테이너를 실행(UP)합니다..."
	docker-compose -p ${DOCKER_APP_NAME}-green -f docker-compose.green.yml up -d --build

	sleep 30

	docker-compose -p ${DOCKER_APP_NAME}-blue -f docker-compose.blue.yml down
	docker image prune -af
fi
