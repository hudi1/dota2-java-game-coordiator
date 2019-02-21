compile project:

`cd ~/git/dota-bot`

`mvn clean install`

push docker image of dota-gc to your local registry:

`cd ~/git/dota-bot/dota-gc`

`mvn clean package docker:build -D pushImage`

run application:

`docker run -it -p 8080:8080 localhost:5000/dota-gc`
