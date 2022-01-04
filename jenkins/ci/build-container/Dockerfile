FROM openjdk:11-bullseye

#RUN apt-get update && apt-get -y install m4 git

WORKDIR /tmp

#RUN git clone https://github.com/SJrX/systemdUnitFilePlugin.git

WORKDIR /tmp/systemdUnitFilePlugin/

RUN ["dd", "if=/dev/zero", "of=/tmp/zero.dat", "bs=1", "count=0", "seek=16384M"]

#RUN ["/tmp/systemdUnitFilePlugin/gradlew","--no-daemon", "dependencies"]