#!/usr/bin/env bash

echo "Git Pull" && \
  git pull && \
  echo "Run jinja2" && \
  python3 ./tools/meson-render-jinja2.py ./build/config.h ./src/version/version.h.in  ./src/core/load-fragment-gperf.gperf.in load-fragment-gperf.gperf &&  \
  echo "Copy file(s)" && \
  cp load-fragment-gperf.gperf /mount/load-fragment-gperf.gperf && \
  cp -R ./man /mount/ && \
  echo "Reset Permissions" && \
  chmod 777 -R /mount
