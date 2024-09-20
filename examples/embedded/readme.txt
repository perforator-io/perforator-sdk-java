This is an example of the load-generator integrated with the Perforator platform
using pure Java logic to process your load testing suite.

Requirements:
- JDK 17+ is required.
- Maven 3.6.3+ is required
- java executable should be available via shell path.
- mvn executable should be available via shell path.

The prebuilt example comes with a set of handy scripts targeted to execute 
your tests with the different goals:
- local.sh - executes your tests using Chrome browsers started locally. 
  This script is helpful to debug and visually verify that everything works as 
  intended.
- local-debug.sh - this script is the same as local.sh, but starts Maven session
  in debug mode awaiting external debugger attachment, so you can connect using 
  your java IDE.
- local-headless.sh - it has the same purpose as local.sh, but valuable for 
  CI/CD environments where a graphical environment is unavailable.
- cloud-dry-run.sh - it processes your tests using only 10 browsers per suite
  started in the cloud. The main idea of this script is to avoid extra charges 
  when you need to ensure that actions are executed correctly in cloud-based 
  browsers. For example, it is not optimal to run your full-powered 
  configuration using thousands of browsers and then, in a minute, realize that 
  you have a simple mistake in css selector.
- cloud-full-run.sh - executes your load test at full speed as defined in 
  config.yml.

There are .cmd equivalents for all .sh scripts, so please use cmd scripts in a 
Windows environment.

Important: Please populate apiClientId, apiClientSecret, and projectKey in
pom.xml before you start playing with this example.

We have prepared a dedicated guide on how to use Standalone Load Generator, which 
is available at https://app.perforator.io/guide/load_generator/standalone
