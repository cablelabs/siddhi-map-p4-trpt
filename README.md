Siddhi Map P4 Telemetry Report
======================================

The **siddhi-map-p4-trpt extension** is an extension to <a target="_blank" href="https://siddhi.io/">Siddhi</a> that converts
A byte array from the payload of a UDP packets representing a P4 Telemetry Report into a TelemetryReport object.
This extension is also capable of mapping each field within Telemetry Report JSON string objects as well.
the field 

For information on <a target="_blank" href="https://siddhi.io/">Siddhi</a> and it's features refer <a target="_blank" href="https://siddhi.io/redirect/docs.html">Siddhi Documentation</a>. 

## Obtain bundle via Maven

Other Siddhi I/O extensions can include this project into their build by adding the following repository and dependency
to the component's pom.xml file.

```xml
    <repositories>
        ...
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
        ...
    </repositories>
```

```xml
    <dependencies>
        ...
        <dependency>
            <groupId>com.github.cablelabs</groupId>
            <artifactId>siddhi-map-p4-trpt</artifactId>
            <version>master-SNAPSHOT</version>
            <scope>test</scope>
        </dependency>
        ...
    </dependencies>
```

## Latest API Docs

## Features

* p4-trpt (Source Mapper) - This extension is capable of parsing and mapping a byte array or JSON string representation
  of a Telemetry Report. 
    
## Dependencies 

There are no other dependencies needed for this extension.

## Installation
   
For installing this extension on various Siddhi execution environments refer Siddhi documentation section on <a target="_blank" href="https://siddhi.io/redirect/add-extensions.html">adding extensions</a>.
   
## Support and Contribution
   
* We encourage users to ask questions and get support via <a target="_blank" href="https://stackoverflow.com/questions/tagged/siddhi">StackOverflow</a>, make sure to add the `siddhi` tag to the issue for better response.

* If you find any issues related to the extension please report them on <a target="_blank" href="https://github.com/siddhi-io/siddhi-map-p4-trpt/issues">the issue tracker</a>.

* For production support and other contribution related information refer <a target="_blank" href="https://siddhi.io/community/">Siddhi Community</a> documentation.