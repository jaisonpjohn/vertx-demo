FROM openjdk:8-jre-alpine
ADD ./build/libs/vertx-demo-0.0.1-fat.jar app.jar
RUN sh -c 'touch /app.jar'
RUN mkdir data
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]