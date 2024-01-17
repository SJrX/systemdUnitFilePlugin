#!/bin/bash

for type in service socket device mount automount swap target path timer slice scope
do
  echo "Processing extension '$type'" >&2
  apt-file search -x  "\.$type$" | grep -v example | grep "systemd"  | grep -v "/test" | sed -E "s#^.+/##g" | sort | uniq
done

echo "All done" >&2