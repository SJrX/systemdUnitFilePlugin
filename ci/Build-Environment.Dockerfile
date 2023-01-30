FROM ubuntu:22.04

RUN apt-get update

RUN apt-get install -y git openjdk-17-jdk-headless

WORKDIR /tmp

RUN useradd -m builduser -u 1000

USER 1000

RUN git clone https://github.com/SJrX/systemdUnitFilePlugin.git

WORKDIR /tmp/systemdUnitFilePlugin

RUN /tmp/systemdUnitFilePlugin/gradlew --no-daemon dependencies
RUN /tmp/systemdUnitFilePlugin/gradlew --no-daemon downloadIdeaProductReleasesXml
RUN /tmp/systemdUnitFilePlugin/gradlew --no-daemon setupDependencies
RUN mkdir -p ./systemd-build/build/man &&  touch ./systemd-build/build/ubuntu-units.txt && touch ./systemd-build/build/load-fragment-gperf.gperf
RUN /tmp/systemdUnitFilePlugin/gradlew --no-daemon prepareSandbox
RUN /tmp/systemdUnitFilePlugin/gradlew --no-daemon tasks
RUN ls
#RUN /tmp/systemdUnitFilePlugin/gradlew --no-daemon  dependencies downloadIdeaProductReleasesXml setupDependencies prepareSandbox compile && rm -rf /tmp/systemdUnitFilePlugin/

WORKDIR /
