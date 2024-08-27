FROM registry.cn-hangzhou.aliyuncs.com/taoduoduo/openjdk:8-jdk-slim

WORKDIR /app

COPY target/schoolmall.jar /app/schoolmall.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/schoolmall.jar"]