java ^
  -Dlog4j2.configurationFile=%~dp0\log4j2.xml ^
  -Dsuite.logActions=true ^
  -Dsuite.logSteps=true ^
  -Dsuite.webDriverMode=local ^
  -Dsuite.concurrency=1 ^
  -Dsuite.duration=5m ^
  -Dsuite.rampUp=1m ^
  -Dsuite.rampDown=1m ^
  %* ^
  -jar %~dp0\%CODELESS_LOAD_GENERATOR_JAR% ^
  %~dp0\config.yml
