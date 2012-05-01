$Id$

Eclipse projects can be configured using the Eclipse plugin for
Maven.  This plugin will be installed upon the first invocation.

The plugin will generate .project, .classpath files and 
.settings directory.  The jar library dependencies will 
relative to the local Maven dependency repository.

To insure for the convenience of being able to debug with a source
code debugger, such as Eclipse, you'll need the corresponding 
source code jars and, optionally, javadoc docs, if available.

To generate these project files, run the following command:

mvn eclipse:clean
mvn eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true

This will use the default Maven profile which build code that can
only run in the emulator or on a device.  If you find that annoying,
there is a profile called "mock-android" which will build code
that can run indepently of the emulator or device.

For mock-android you need a dependency that is unavailable in any
pulic Maven repository, the xpp3 pull parser.  Get it from:

http://www.extreme.indiana.edu/dist/java-repository/xpp3/distributions

Then from the build/lib directory, run these commands:

mvn install:install-file -Dfile=xpp3-1.1.3.4.D.jar -DgroupId=extreme_xpp3 \
    -DartifactId=xpp3 -Dversion=1.1.3.4.D -Dpackaging=jar \
    -DgeneratePom=true
mvn install:install-file -Dfile=xpp3-1.1.3.4.D-sources.jar \
    -DgroupId=extreme_xpp3 -DartifactId=xpp3 -Dversion=1.1.3.4.D \
    -Dpackaging=java-source -DgeneratePom=false


Once that's installed, build the Android-mock project:

cd ./AndroidSOAP/Android-mock
mvn install

This will build and install adroid-mock.jar


Finally run these commands to configure Eclipse to use mock-android

mvn eclipse:eclipse -DdownloadSources=true \
    -DdownloadJavadocs=true -P\!default,mock-android

After this, go into Eclipse:
  preferences => Java => Build Path => Classpath Variables
and create and set a 
classpath variable, "M2_REPO" and point it to your local Maven 
dependencies reposity root directory.  On Mac/Linux systems, 
this is typically "~/.m2/repository"
