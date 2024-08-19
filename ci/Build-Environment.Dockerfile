FROM ubuntu:22.04

RUN apt-get update

RUN apt-get install -y git openjdk-21-jdk-headless

WORKDIR /tmp

RUN useradd -m builduser -u 1000

USER 1000

RUN git clone https://github.com/SJrX/systemdUnitFilePlugin.git

WORKDIR /tmp/systemdUnitFilePlugin

RUN /tmp/systemdUnitFilePlugin/gradlew --no-daemon --build-cache dependencies compileKotlin && rm -rf /tmp/systemdUnitFilePlugin/

WORKDIR /
