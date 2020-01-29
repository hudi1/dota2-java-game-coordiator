FROM arm32v7/openjdk

ADD @project.build.finalName@.war /usr/local/

WORKDIR /usr/local

EXPOSE 7001

ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Duser.timezone=Europe/Prague -Djava.security.egd=file:/dev/./urandom -jar @project.build.finalName@.war" ]