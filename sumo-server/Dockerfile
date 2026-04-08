# install sumo
# for whatever reason, the sumo ubuntu package for arm64 doesn't include libtracijni, hence why we need to compile it from scratch
FROM ubuntu:jammy AS sumo-builder
ARG SUMO_VERSION="1.18.0"
ENV SUMO_HOME="/opt/sumo"
RUN apt-get update && apt-get install -y git cmake python3 g++ libxerces-c-dev libfox-1.6-dev libgdal-dev libproj-dev libgl2ps-dev python3-dev swig default-jdk maven libeigen3-dev curl
WORKDIR /opt
RUN curl -OJL "https://sumo.dlr.de/releases/$SUMO_VERSION/sumo-src-$SUMO_VERSION.tar.gz"
RUN tar xvf sumo-src-$SUMO_VERSION.tar.gz
RUN mv sumo-$SUMO_VERSION sumo
WORKDIR $SUMO_HOME
RUN mkdir -p build/cmake-build
WORKDIR $SUMO_HOME/build/cmake-build
RUN cmake ../..
RUN make -j$(nproc)

FROM gradle:8-jdk17-jammy AS gradle-builder
ENV SUMO_HOME="/opt/sumo"
ENV PATH="/opt/sumo/bin:${PATH}"
ENV JAVA_TOOL_OPTIONS="-Djava.library.path=/opt/sumo/bin"
RUN apt-get update && apt-get install -y libxerces-c3.2 libproj22 libfox-1.6-0 libx11-6 libxext6 libxft2 libxcursor1 libgl1 libglu1-mesa libjpeg62 libtiff5 libgdal30
# copy sumo
WORKDIR $SUMO_HOME/bin
COPY --from=sumo-builder /opt/sumo/bin .
WORKDIR /opt/urbanflo-sumo-server
COPY . .
RUN gradle build

FROM eclipse-temurin:17-jammy AS urbanflo-sumo-server
ARG VERSION="0.0.1-SNAPSHOT"
ENV SUMO_HOME="/opt/sumo"
ENV PATH="/opt/sumo/bin:${PATH}"
RUN apt-get update && apt-get install -y libxerces-c3.2 libproj22 libfox-1.6-0 libx11-6 libxext6 libxft2 libxcursor1 libgl1 libglu1-mesa libjpeg62 libtiff5 libgdal30
# copy sumo
WORKDIR $SUMO_HOME/bin
COPY --from=sumo-builder /opt/sumo/bin .
WORKDIR /opt/urbanflo-sumo-server
# copy server jar
COPY --from=gradle-builder /opt/urbanflo-sumo-server/build/libs/urbanflo-sumo-server-$VERSION.jar urbanflo-sumo-server.jar

EXPOSE 80
CMD ["java", "-jar", "-Djava.library.path=/opt/sumo/bin", "-Dspring.profiles.active=prod", "/opt/urbanflo-sumo-server/urbanflo-sumo-server.jar"]