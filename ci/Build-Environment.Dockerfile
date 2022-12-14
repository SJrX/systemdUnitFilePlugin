FROM ubuntu:22.04

RUN apt-get update

RUN apt-get install -y git openjdk-17-jdk-headless

WORKDIR /tmp

RUN git clone https://github.com/SJrX/systemdUnitFilePlugin.git

WORKDIR /tmp/systemdUnitFilePlugin
RUN /tmp/systemdUnitFilePlugin/gradlew --no-daemon dependencies && rm -rf /tmp/systemdUnitFilePlugin/
