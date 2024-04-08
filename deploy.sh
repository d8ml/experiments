#!/bin/bash

start=$(date +"%s")

ssh -p ${SERVER_PORT} ${SERVER_USER}@${SERVER_HOST} -i key.txt -t -t -o StrictHostKeyChecking=no << 'ENDSSH'
sudo docker pull d8ml/forest_of_habits

CONTAINER_NAME=forest_of_habits
if [ "$(sudo docker ps -qa -f name=$CONTAINER_NAME)" ]; then
    if [ "$(sudo docker ps -q -f name=$CONTAINER_NAME)" ]; then
        echo "Container is running -> stopping it..."
        sudo docker stop $CONTAINER_NAME;
    fi
fi

cd docker/
sudo docker compose up -d

exit
ENDSSH

if [ $? -eq 0 ]; then
  exit 0
else
  exit 1
fi

end=$(date +"%s")

diff=$(($end - $start))

echo "Deployed in : ${diff}s"
