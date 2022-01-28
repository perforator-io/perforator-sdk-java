#!/bin/sh

java \
  -Dlog4j2.configurationFile=log4j2.xml \
  -Dsuite.webDriverMode=cloud \
  $@ \
  -jar ${CODELESS_LOAD_GENERATOR_JAR} \
  config.yml