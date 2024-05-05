FROM ubuntu:22.04

ENV DEBIAN_FRONTEND noninteractive

RUN ln -fs /usr/share/zoneinfo/America/Vancouver /etc/localtime

RUN apt-get update && apt-get -y install git build-essential tzdata meson pkg-config gperf python3-jinja2 libcap-dev util-linux libmount1 libmount-dev


RUN mkdir /opt/systemd-source

RUN git clone https://github.com/systemd/systemd.git /opt/systemd-source/systemd

WORKDIR /opt/systemd-source/systemd

RUN meson setup build

RUN mkdir -p /mount/

ADD systemd-build.sh /

CMD /systemd-build.sh

RUN /systemd-build.sh
