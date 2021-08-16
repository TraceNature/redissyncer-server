#MAINTAINER "zhanenqiang"<1604030622@qq.com>
#ADD syncerplus-webapp-0.7.jar app.jar
#EXPOSE 8080
#CMD java -jar app.jar

# 基础镜像
FROM openjdk:8u282-jdk

#ENV JAVA_TOOL_OPTIONS=" -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Xms512m -Xmx2G"
ENV JAVA_TOOL_OPTIONS=" -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 -Xms512m"
ENV SPRING_ENV="--server.port=80 --syncer.config.path.logfile=/log --syncer.config.path.datafile=/data"

# 对应pom.xml文件中的dockerfile-maven-plugin插件buildArgs配置项JAR_FILE的值
ARG JAR_FILE
# 复制打包完成后的jar文件到/opt目录下
#COPY ${JAR_FILE}  /opt/app.jar
RUN mkdir -p /opt/redissyncer && mkdir -p /log && mkdir -p /data
COPY syncer-webapp/${JAR_FILE} /opt/redissyncer/redissyncer.jar
# 启动容器时执行
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/app.jar"]
#CMD java -jar /opt/redissyncer/redissyncer.jar --server.port=80
CMD java -jar /opt/redissyncer/redissyncer.jar ${SPRING_ENV} && 1

# 使用端口80
EXPOSE 80
