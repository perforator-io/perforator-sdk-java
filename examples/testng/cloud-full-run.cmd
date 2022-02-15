mvn -f %~dp0\pom.xml ^
  clean test-compile ^
  perforator:testng ^
  -Dsuite.webDriverMode=cloud ^
  %*