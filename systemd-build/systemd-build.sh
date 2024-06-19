#!/usr/bin/env bash


echo "Git Pull" && \
  git pull && \
  echo "Run jinja2" && \
  python3 ./tools/meson-render-jinja2.py ./build/config.h ./src/core/load-fragment-gperf.gperf.in load-fragment-gperf.gperf &&  \
  echo "Copy file(s)" && \
  cp load-fragment-gperf.gperf /mount/load-fragment-gperf.gperf && \
  cp -R ./man /mount/ && \
  git log --format="%at" | sort | tail -n 1 | xargs -I{} date -d @{} +%Y-%m-%d > last_commit_date && \
  git rev-parse --short=10 HEAD > last_commit_hash && \
  cp last_commit_date last_commit_hash /mount/ && \
  echo "Reset Permissions" && \
  chmod 777 -R /mount
