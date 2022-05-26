#!/bin/sh

mvn -f $(dirname "$0")/pom.xml \
  clean test-compile \
  perforator:embedded \
  -Dsuite.webDriverMode=cloud \
  "$@"