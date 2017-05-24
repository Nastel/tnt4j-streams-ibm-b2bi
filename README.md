# tnt4j-streams-ibm-b2bi
TNT4J Streams for IBM Sterling B2B Integrator.

TNT4J-Streams-IBM-B2Bi is extension of TNT4J-Streams to give ability of streaming IBM Sterling B2B Integrator events as activity events to 
JKoolCloud.

TNT4J-Streams-IBM-B2Bi is under Apache v2.0 license.

This document covers just information specific to TNT4J-Streams-IBM-B2Bi project.
Detailed information on TNT4J-Streams can be found in [README document](https://github.com/Nastel/tnt4j-streams/blob/master/README.md).

Why TNT4J-Streams-IBM-B2Bi
======================================

 * Plugs into IBM Sterling B2B Integrator as `com.sterlingcommerce.woodstock.event.EventListener`.
 
Many components in the B2Bi/SFG system generate events during the course of normal processing. In the case of SFG, all of the visibility 
data shown in the SFG tracking UI is raised as event data and sent to JKoolCloud via an event listener. Each event is sent through an 
in-memory concurrent queue data structure.
 
Protocol adapters also generate events during connect, disconnect and file transfer. 
 
Importing TNT4J-Streams-IBM-B2Bi project into IDE
======================================

## Eclipse
* Select File->Import...->Maven->Existing Maven Projects
* Click 'Next'
* In 'Root directory' field select path of directory where You have downloaded (checked out from git)
TNT4J-Streams project
* Click 'OK'
* Dialog fills in with project modules details
* Click 'Finish'

Running TNT4J-Streams-IBM-B2Bi
======================================

Also see TNT4J-Streams README document chapter ['Running TNT4J-Streams'](https://github.com/Nastel/tnt4j-streams/blob/master/README.md#running-tnt4j-streams).

## TNT4J-Streams-IBM-B2Bi can be run
* As API integrated into IBM Sterling B2B Integrator
    * Build a jar file containing the implementation (including the complete package directory structure)
    * Run IBM Sterling B2B Integrator `InstallThirdParty.sh` to put the new jars into the classpath
    * Once an event listener is created and made available on the classpath, a simple property change is needed to enable the event listener.
    Update IBM Sterling B2B Integrator configuration file `sterling_install_location/install/properties/listenerStartup.properties` entry 
    for listener:
        ```properties
              ## PROPERTY_START
              ## PROPERTY_NAME: Listener.Class.cfx
              ## PROPERTY_TYPE: String
              ## PROPERTY_DESCRIPTION
              ## CFX Event Listeners
              Listener.Class.jkoolcloud=com.jkoolcloud.tnt4j.streams.custom.inputs.B2BiSfgEventStream
              ## PROPERTY_END
        ```
        **Note:** You can comment out this entry to disable the listener.          
    * Write stream parsers configuration file. See ['Streams configuration'](https://github.com/Nastel/tnt4j-streams/blob/master/README.md#streams-configuration) chapter for more details
    * Configure Java system properties for IBM Sterling B2B Integrator startup:
        * `streams.config` - referring parser configuration. See `./samples/B2Bi/tnt-data-source.xml`
        * `log4j.configuration` - referring logger configuration. See `./config/log4j.properties`
        * `tnt4j.config` - referring TNT4J configuration. See `./config/tnt4j.properties`
         **NOTE:** Do not forget to set cloud TOKEN value by changing property `event.sink.factory.Token`.
    
How to Build TNT4J-Streams-IBM-B2Bi
=========================================

## Requirements
* JDK 1.7+
* [Apache Maven 3](https://maven.apache.org/)
* [TNT4J-Streams](https://github.com/Nastel/tnt4j-streams) `core` module in particular

All other required dependencies are defined in project [`pom.xml`](./pom.xml) file. If maven is running online mode it should download these 
defined dependencies automatically.

### Manually installed dependencies

**NOTE:** If you have build and installed TNT4J-Streams into Your local maven repository, you don't need to install
it manually.

Some of required and optional dependencies may be not available in public [Maven Repository](http://repo.maven.apache.org/maven2/). In this 
case we would recommend to download those dependencies manually into [`lib`](./lib/) directory and install into local maven repository by 
running maven script [`lib/pom.xml`](./lib/pom.xml) using `install` goal.

**NOTE:** `TNT4J-Streams-IBM-B2Bi` project will be ready to build only when manually downloaded libraries will be installed to local maven 
repository.

What to download manually or copy from your existing IBM Sterling B2B Integrator installation:
* platform_ifcbase
* install_foundation

Download the above libraries and place into the `tnt4j-streams-ibm-b2bi/lib` directory like this:
```
    lib
     |- platform_ifcbase.jar
     |- install_foundation.jar (O)
```
(O) marked libraries are optional

**NOTE:** also see TNT4J-Streams README document chapter ['Manually installed dependencies'](https://github.com/Nastel/tnt4j-streams/blob/master/README.md#manually-installed-dependencies).

## Building
   * to build project and make release assemblies run maven goals `clean package`
   * to build project, make release assemblies and install to local repo run maven goals `clean install`

Release assemblies are built to `../build/tnt4j-streams-ibm-b2bi` directory.

**NOTE:** sometimes maven fails to correctly handle dependencies. If dependency configuration looks fine, but maven still complains about 
missing dependencies try to delete local maven repository by hand: i.e. on MS Windows delete contents of `c:\Users\[username]\.m2\repository` 
directory.

So resuming build process quick "how to build" steps would be like this:
1. download `platform_ifcbase.jar` and `install_foundation.jar` to `tnt4j-streams-ibm-b2bi/lib` directory.
2. install manually managed dependencies from `tnt4j-streams-ibm-b2bi/lib` directory running `mvn install`.
3. if `tnt4j-streams` not built yet build it: run `mvn clean install` for a [`pom.xml`](https://github.com/Nastel/tnt4j-streams/blob/master/pom.xml) 
file located in `tnt4j-streams` directory. 
4. now you can build `tnt4j-streams-ibm-b2bi`: run `mvn clean install` for a [`pom.xml`](./pom.xml) file located in `tnt4j-streams-ibm-b2bi` 
directory. 

## Running samples

See 'Running TNT4J-Streams-IBM-B2Bi' chapter section ['Samples'](#samples).

Testing of TNT4J-Streams-IBM-B2Bi
=========================================

## Requirements
* [JUnit 4](http://junit.org/)
* [Mockito](http://mockito.org/)
* [PowerMock](http://powermock.github.io/)

## Testing using maven
Maven tests run is disabled by default. To enable Maven to run tests set Maven command line argument 
`-DskipTests=false`.

## Running manually from IDE
* in `ibm-b2bi` module run JUnit test suite named `AllB2BiStreamTests`
