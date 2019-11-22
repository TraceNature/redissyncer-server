#FROM java:8
#MAINTAINER "zhanenqiang"<1604030622@qq.com>
#ADD syncerplus-webapp-0.7.jar app.jar
#EXPOSE 8080
#CMD java -jar app.jar

# 基础镜像
FROM openjdk:8-jdk-alpine
# 对应pom.xml文件中的dockerfile-maven-plugin插件buildArgs配置项JAR_FILE的值
ARG JAR_FILE
# 复制打包完成后的jar文件到/opt目录下
COPY ${JAR_FILE}  /opt/app.jar
# 启动容器时执行
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/app.jar"]
# 使用端口80
EXPOSE 80