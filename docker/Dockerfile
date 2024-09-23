FROM openjdk:17.0.2-oraclelinux8

MAINTAINER xpy
RUN mkdir -p /server/bing_wallpaper/logs \
    /server/bing_wallpaper/temp \
    /server/bing_wallpaper/images
WORKDIR /server/bing_wallpaper
ENV SERVER_PORT=18083

EXPOSE ${SERVER_PORT}

ADD ./bingWallpaper-api.jar ./app.jar

ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Dserver.port=${SERVER_PORT}", \
            "-jar", "app.jar", \
            "--spring.profiles.active=test"]
