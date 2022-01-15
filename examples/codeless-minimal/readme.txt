This is an example of the load-generator integrated with the Perforator platform
in a codeless way, i.e., no Java programming skills are required.
You only need to populate configuration in config.yml file and run it via shell 
script(s).

Requirements:
- JDK 11+ is required.
- java executable should be available via shell path.

The prebuilt example comes with a set of handy scripts targeted to execute 
config.yml with the different goals:
- local.sh - executes your config.yml using Chrome browsers started locally. 
  This script is helpful to debug and visually verify that everything works as 
  intended.
- local-headless.sh - it has the same purpose as local.sh, but valuable for 
  CI/CD environments where a graphical environment is unavailable.
- cloud-dry-run.sh - it processes your configuration file using only 10 browsers 
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
config.yml before you start playing with this example.

We have prepared a dedicated guide on how to use Codeless Load Generator, which 
is available at https://app.perforator.io/guide/load_generator/codeless
