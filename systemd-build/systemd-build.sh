#!/usr/bin/env bash

echo "Git Pull" && \
  git reset --hard HEAD &&\
  git pull && \
  find . -type f -name meson.build -exec sed -i 's/install_emptydir(\(.*\), install_tag : .*)/install_emptydir(\1)/g' '{}' '+' && \
  echo "Patching config.h" && \
  sed -i -E "s/HAVE_SECCOMP 0/HAVE_SECCOMP 1/g" ./build/config.h && \
  echo "Run jinja2" && \
  python3 ./tools/meson-render-jinja2.py ./build/config.h ./src/core/load-fragment-gperf.gperf.in load-fragment-gperf.gperf &&  \
  echo "Copy file(s)" && \
  cp load-fragment-gperf.gperf /mount/load-fragment-gperf.gperf && \
  cp ./src/journal/journald-gperf.gperf /mount/ && \
  cp ./src/login/logind-gperf.gperf /mount/ && \
  cp ./src/nspawn/nspawn-gperf.gperf /mount/ && \
  cp ./src/timesync/timesyncd-gperf.gperf /mount/ && \
  cp ./src/udev/net/link-config-gperf.gperf /mount/ && \
  cp ./src/resolve/resolved-gperf.gperf /mount/ && \
  cp ./src/resolve/resolved-dnssd-gperf.gperf /mount/  && \
  cp ./src/network/netdev/netdev-gperf.gperf /mount/  && \
  cp ./src/network/networkd-network-gperf.gperf /mount/  && \
  cp ./src/network/networkd-gperf.gperf /mount/  && \
  cp -R ./man /mount/ && \
  /usr/bin/meson --internal exe --capture /mount/man/ethtool-link-mode.xml -- /usr/bin/python3 ./src/shared/ethtool-link-mode.py --xml 'cc -E' ./src/basic/linux/ethtool.h && \
  git log --format="%at" | sort | tail -n 1 | xargs -I{} date -d @{} +%Y-%m-%d > last_commit_date && \
  git rev-parse --short=10 HEAD > last_commit_hash && \
  cp last_commit_date last_commit_hash /mount/ && \
  echo "Reset Permissions" && \
  chmod 777 -R /mount
