mvn -f %~dp0\pom.xml ^
  clean test-compile ^
  perforator:embedded ^
  -Dsuite.webDriverMode=cloud ^
  %*