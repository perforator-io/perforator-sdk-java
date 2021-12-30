This is an example of the load-generator integrated with the Perforator platform
in a codeless way, i.e., no Java programming skills are required.
You only need to populate configuration in config.yml file and run it via shell 
script(s).

Requirements:
- JDK 11+ is required.
- java executable should be available via shell path.

It is encouraged to use predefined flow when you prepare a load-test:
1. Make changes in config.yml
2. Verify that all the changes are valid and browser executes your actions as intended.
   Please use local.cmd on Windows systems or local.sh on Linux / MacOS systems.
   Executing such shell script(s) should process your config file using browsers
   started locally, so you can visually verify and debug what happens in the browser.
   If you are working in a headless environment, for example, using a CI/CD 
   pipeline, you can use local-headless.cmd / local-headless.sh
3. Please execute cloud-dry-run shell script to make sure that your config works
   as expected using browsers launched in the cloud. It runs config.yml using a 
   very limited concurrency of 10 browsers in the cloud for 5 minutes, so you 
   don't spend too many credits while verifying your changes.
4. Please execute cloud-full-run shell script, and it will run a performance test 
   at full speed once you are done with all verifications.

- Windows:
  - local.cmd - run config.yml using local Chrome browser.
  - local-headless.cmd - run config.yml using local headless Chrome browser.
  - cloud-dry-run.cmd - run config.yml using browsers in the cloud, but with a limited concurrency.
  - cloud-full-run.cmd - run config.yml using browsers in the cloud at full speed.
- Linux / MacOS:
  - local.sh - run config.yml using local Chrome browser.
  - local-headless.sh - run config.yml using local headless Chrome browser.
  - cloud-dry-run.sh - run config.yml using browsers in the cloud, but with a limited concurrency.
  - cloud-full-run.sh - run config.yml using browsers in the cloud at full speed.
