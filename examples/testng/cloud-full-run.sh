#!/bin/sh

mvn -f $(dirname "$0")/pom.xml \
  clean test-compile \
  perforator:testng \
  -Dsuite.webDriverMode=cloud \
  $@