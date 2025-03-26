FROM ubuntu:22.04

RUN apt-get update

RUN apt-get install -y git openjdk-17-jdk-headless

WORKDIR /tmp

RUN useradd -m builduser -u 1001

USER 1001

RUN git clone https://github.com/SJrX/systemdUnitFilePlugin.git -b 223.x

WORKDIR /tmp/systemdUnitFilePlugin

RUN /tmp/systemdUnitFilePlugin/gradlew --no-daemon --build-cache dependencies compileKotlin && rm -rf /tmp/systemdUnitFilePlugin/

WORKDIR /
