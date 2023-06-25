java ^
  -Dlog4j2.configurationFile=%~dp0\log4j2.xml ^
  -Dsuite.webDriverMode=cloud ^
  -Dsuite.concurrency=16 ^
  -Dsuite.duration=5m ^
  -Dsuite.rampUp=1m ^
  -Dsuite.rampDown=1m ^
  %* ^
  -jar %~dp0\%CODELESS_LOAD_GENERATOR_JAR% ^
  %~dp0\config.yml