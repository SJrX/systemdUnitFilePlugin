FROM openjdk:11-bullseye

RUN apt-get update && apt-get -y install m4

WORKDIR /tmp

RUN git clone https://github.com/SJrX/systemdUnitFilePlugin.git

WORKDIR /tmp/systemdUnitFilePlugin/

RUN /tmp/systemdUnitFilePlugin/gradlew --no-daemon dependencies && rm -rf /tmp/systemdUnitFilePlugin/

WORKDIR /