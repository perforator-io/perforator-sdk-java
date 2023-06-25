mvn -f %~dp0\pom.xml ^
  clean test-compile ^
  perforator:testng ^
  -Dsuite.webDriverMode=cloud ^
  -Dsuite.concurrency=16 ^
  -Dsuite.duration=5m ^
  -Dsuite.rampUp=1m ^
  -Dsuite.rampDown=1m ^
  %*