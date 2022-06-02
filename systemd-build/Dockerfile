FROM ubuntu:22.04

ENV DEBIAN_FRONTEND noninteractive

RUN ln -fs /usr/share/zoneinfo/America/Vancouver /etc/localtime

RUN apt-get update && apt-get -y install git build-essential tzdata meson pkg-config gperf python3-jinja2 libcap-dev util-linux libmount1 libmount-dev

RUN mkdir /opt/systemd-source

RUN git clone https://github.com/systemd/systemd.git /opt/systemd-source/systemd

WORKDIR /opt/systemd-source/systemd

RUN ./configure

CMD echo "Git Pull" && \
  git pull && \
  echo "Run jinja2" && \
  python3 ./tools/meson-render-jinja2.py ./build/config.h ./src/version/version.h.in  ./src/core/load-fragment-gperf.gperf.in load-fragment-gperf.gperf &&  \
  echo "Copy file(s)" && \
  cp load-fragment-gperf.gperf /mount/load-fragment-gperf.gperf && \
  cp -R ./man /mount/ && \
  echo "Reset Permissions" && \
  chmod 777 -R /mount