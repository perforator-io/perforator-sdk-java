java ^
  -Dsuite.webDriverMode=local ^
  -Dsuite.concurrency=5 ^
  -Dsuite.duration=5m ^
  -Dsuite.rampUp=1m ^
  -Dsuite.rampDown=1m ^
  -jar %CODELESS_LOAD_GENERATOR_JAR% ^
  config.yml
