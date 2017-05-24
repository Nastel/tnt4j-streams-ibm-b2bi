# tnt4j-streams-ibm-b2bi
TNT4J Streams for IBM Sterling B2B Integrator

Many components in the B2Bi/SFG system generate events during the course of normal processing.  In the case of SFG, all of the visibility data shown in the SFG tracking UI is raised as event data and sent to JKollCloud via an event listener.  Each event is sent through an in-memory concurrent queue data structure.
 
Protocol adapters also generate events during connect, disconnect and file transfer.
    
## Prerequisites

* Apcahe maven 
* Installation of IBM Sterling

## Building 

1. Install required IBM lib's from `./lib` directory using Maven.
	1.1 `cd lib`
	1.2 `mvn install`
	1.3 `cd ..`
2. Run your Maven using command `mvn package`.

## Install

1. Run InstallThirdParty.sh to put the new jar into the classpath.  

2. Once an event listener is created and made available on the classpath, a simple property change is needed to enable the event listener:

Update entry for listener in install_location/install/properties/listenerStartup.properties

```    
	## PROPERTY_START
    ## PROPERTY_NAME: Listener.Class.cfx
    ## PROPERTY_TYPE: String
    ## PROPERTY_DESCRIPTION
    ## CFX Event Listeners
    Listener.Class.sample=com.jkoolcloud.tnt4j.streams.custom.inputs.B2BiSfgEventStream
    ## PROPERTY_END
	
```
   Note: You can comment out this entry to disable the listener
   
   