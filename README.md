compile project:

`cd ~/git/dota-bot`

`mvn clean install`

docker:

push docker image of dota-gc to your local registry:

`cd ~/git/dota-bot/dota-gc`

`mvn clean package docker:build -D pushImage`

run application:

`docker run -it -p 8080:8080 localhost:5000/dota-gc`

localhost:

`mvn spring-boot:run`

`http -a admin:admin123 GET http://localhost:7001/dota-gc/rest/steam/client/config funkce==setAuthCode klient==admin123 args==xxxx`
`http -a admin:admin123 GET http://localhost:7001/dota-gc/rest/steam/client funkce==connect klient==admin123`
