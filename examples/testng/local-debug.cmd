mvnDebug -f %~dp0\pom.xml ^
  clean test-compile ^
  perforator:testng ^
  -Dsuite.webDriverMode=local ^
  -Dsuite.concurrency=1 ^
  -Dsuite.duration=5m ^
  -Dsuite.rampUp=1m ^
  -Dsuite.rampDown=1m ^
  %*
