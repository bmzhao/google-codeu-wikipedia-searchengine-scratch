#!/usr/bin/env bash

docker run -d -p 3306:3306 --name mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=Crawl mysql:5.5
