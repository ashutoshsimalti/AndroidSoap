$Id$

Here is how you can run unit tests that will run independently
of an emulator or device (phone) :

mvn test -P \!default,mock-android

The unit test results are placed in:

./target/surefire-reports/*.txt
./target/surefire-reports/*.xml

To run unit tests and generate HTML reports, run this command:
mvn surefire-report:report -P \!default,mock-android


...if you already ran the unit tests (test goal) you can
perform a report-only run like this:

mvn surefire-report:report-only -P \!default,mock-android


The test reports are in:
./target/site/surefire-reports.html
