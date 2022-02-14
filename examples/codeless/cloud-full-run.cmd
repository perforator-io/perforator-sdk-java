java ^
  -Dlog4j2.configurationFile=%~dp0\log4j2.xml ^
  -Dsuite.webDriverMode=cloud ^
  %* ^
  -jar %~dp0\%CODELESS_LOAD_GENERATOR_JAR% ^
  %~dp0\config.yml