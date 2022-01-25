java ^
  -Dlog4j2.configurationFile=log4j2.xml ^
  -Dsuite.webDriverMode=local ^
  -Dsuite.chromeMode=headless ^
  -Dsuite.concurrency=1 ^
  -Dsuite.duration=5m ^
  -Dsuite.rampUp=1m ^
  -Dsuite.rampDown=1m ^
  %* ^
  -jar %CODELESS_LOAD_GENERATOR_JAR% ^
  config.yml
