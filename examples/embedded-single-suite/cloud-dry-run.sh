#!/bin/sh

mvn clean test-compile perforator:embedded \
  -Dsuite.webDriverMode=cloud \
  -Dsuite.concurrency=10 \
  -Dsuite.duration=5m \
  -Dsuite.rampUp=1m \
  -Dsuite.rampDown=1m \
  $@