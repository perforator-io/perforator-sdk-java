#!/bin/sh

java \
  -Dlog4j2.configurationFile=$(dirname "$0")/log4j2.xml \
  -Dsuite.webDriverMode=cloud \
  "$@" \
  -jar $(dirname "$0")/${CODELESS_LOAD_GENERATOR_JAR} \
  $(dirname "$0")/config.yml