FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN ln -fs /usr/share/zoneinfo/Etc/UTC /etc/localtime

RUN apt-get update && apt-get -y install git build-essential tzdata meson pkg-config gperf python3-jinja2 libcap-dev util-linux libmount1 libmount-dev

RUN mkdir /opt/systemd-source

RUN git clone https://github.com/systemd/systemd.git /opt/systemd-source/systemd

WORKDIR /opt/systemd-source/systemd

RUN mkdir -p /mount/

# Force cache to be invalidated after this point
ARG BUILDDATE
ENV BUILDDATE=${BUILDDATE:-notset}

RUN git pull

# https://github.com/systemd/systemd/commit/8442ac9c0264ac7beb5afd6c3bf922030a6edaf3
# TODO I think this can be removed we had to upgrade to ubuntu 24.04 and this was something with an old version of the build tool in ubuntu 22.04
RUN find . -type f -name meson.build -exec sed -i 's/install_emptydir(\(.*\), install_tag : .*)/install_emptydir(\1)/g' '{}' '+'

RUN meson setup build

ADD systemd-build.sh /

CMD /systemd-build.sh

RUN /systemd-build.sh

