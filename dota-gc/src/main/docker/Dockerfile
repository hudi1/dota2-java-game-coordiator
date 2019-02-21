FROM reg-5.i.cz:5000/openjdk:11-jdk-slim

ADD @project.build.finalName@.war /usr/local/
WORKDIR /usr/local

EXPOSE 8080

ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Duser.timezone=Europe/Prague -Djava.security.egd=file:/dev/./urandom -jar @project.build.finalName@.war" ]