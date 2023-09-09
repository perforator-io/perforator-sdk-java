#!/bin/sh

java \
  -Dlog4j2.configurationFile=$(dirname "$0")/log4j2.xml \
  -Dsuite.logActions=true \
  -Dsuite.logSteps=true \
  -Dsuite.webDriverMode=local \
  -Dsuite.chromeMode=headless \
  -Dsuite.concurrency=1 \
  -Dsuite.duration=5m \
  -Dsuite.rampUp=1m \
  -Dsuite.rampDown=1m \
  "$@" \
  -jar $(dirname "$0")/${CODELESS_LOAD_GENERATOR_JAR} \
  $(dirname "$0")/config.yml
