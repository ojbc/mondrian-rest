Overview
--------

This repository contains a [Spring Boot](https://projects.spring.io/spring-boot/) -based REST API for querying relational [OLAP](https://en.wikipedia.org/wiki/Online_analytical_processing) data sources via [Mondrian](http://community.pentaho.com/projects/mondrian/).

Motivation
----------

The Mondrian library is a terrific way to submit [MDX](https://en.wikipedia.org/wiki/MultiDimensional_eXpressions) queries to relational data sources.  Great interface platforms, like
[Saiku](http://community.meteorite.bi/) and [Pivot4J](http://www.pivot4j.org/) provide full-featured ad-hoc queries for end-users comfortable with [pivot tables](https://en.wikipedia.org/wiki/Pivot_table).  There are also toolkits like [Pentaho CTools](http://community.pentaho.com/ctools/) for developing dashboards.

What has been missing is a simple, basic, and clean REST API wrapped around the Mondrian library.  Each of the tools just mentioned uses its own approach for programmatic access to Mondrian, and does not really make that approach available (in an easy way) outside of that tool.  Thus, we decided to implement the mondrian-rest API.

Usage
-----

#### Via Docker

There is a pre-built Docker image on DockerHub with the API installed.  Right now, it only exposes the FoodMart database/schema, and a small database/schema that we used for testing.

We will soon push a capability to set up arbitrary connections, JDBC drivers, and Mondrian schemas.  Stay tuned!  But for now, the Docker image is suitable for trying out the API.

To run it in a local Docker environment:

`docker run -d --name=mondrian-rest -p 58080:80 ojbc/mondrian-rest` (replace 58080 with whatever port you prefer)

The API will then be running in Docker, exposed on that port, within Apache Tomcat.  See below for API usage.

#### API Usage

There are currently three operations available in the API:

`/getConnections`: Returns information about the olap4j connections currently available to the API

Example:

```
$: curl http://localhost:58080/mondrian-rest/getConnections
{
  "test" : {
    "JdbcDrivers" : "org.hsqldb.jdbc.JDBCDriver",
    "Jdbc" : "jdbc:hsqldb:res:test",
    "Description" : "Main version of test connection",
    "ConnectionDefinitionSource" : "/opt/tomcat/webapps/mondrian-rest/WEB-INF/classes/mondrian-connections.json",
    "MondrianSchemaUrl" : "file:/opt/tomcat/webapps/mondrian-rest/WEB-INF/classes/test.xml"
  },
  "foodmart" : {
    "JdbcDrivers" : "org.hsqldb.jdbc.JDBCDriver",
    "Jdbc" : "jdbc:hsqldb:res:foodmart;set schema \"foodmart\"",
    "Description" : "Pentaho/Hyde FoodMart Database",
    "ConnectionDefinitionSource" : "/opt/tomcat/webapps/mondrian-rest/WEB-INF/classes/mondrian-connections.json",
    "MondrianSchemaUrl" : "https://raw.githubusercontent.com/pentaho/mondrian/lagunitas/demo/FoodMart.mondrian.xml"
  }
}
$:
```

`/getSchema?connectionName=[name]`: Retrieves the XML for the Mondrian schema for the specified connection

Example:

```
$: curl -s http://localhost:58080/mondrian-rest/getSchema?connectionName=foodmart | head -30
<?xml version='1.0'?>
<Schema name='FoodMart' metamodelVersion='4.0'>
    <!--
    == This software is subject to the terms of the Eclipse Public License v1.0
    == Agreement, available at the following URL:
    == http://www.eclipse.org/legal/epl-v10.html.
    == You must accept the terms of that agreement to use this software.
    ==
    == Copyright (C) 2000-2005 Julian Hyde
    == Copyright (C) 2005-2013 Pentaho and others
    == All Rights Reserved.
    -->

    <PhysicalSchema>
        <Table name='salary'/>
        <Table name='salary' alias='salary2'/>
        <Table name='department'>
            <Key>
                <Column name='department_id'/>
            </Key>
        </Table>
        <Table name='employee'>
            <Key>
                <Column name='employee_id'/>
            </Key>
        </Table>
        <Table name='employee_closure'>
            <Key>
                <Column name='employee_id'/>
            </Key>
$:
```

`/query`: POST operation that takes a two-element map.  Key `connectionName` specifies the connection to query, and key `query` specifies the MDX for the query.

Example:

```
$: curl -s -X POST -H 'Content-Type: application/json' http://localhost:58080/mondrian-rest/query -d '{"connectionName" : "foodmart", "query" : "select { [Measures].[Units Shipped] } on columns, NON EMPTY [Store].[Store Type].members on rows from Warehouse" }'
{
  "cells" : [ {
    "formattedValue" : "207726.0",
    "value" : 207726,
    "ordinal" : 0,
    "coordinates" : [ 0, 0 ],
    "error" : null
  }, {
    "formattedValue" : "64804.0",
    "value" : 64804.0,
    "ordinal" : 1,
    "coordinates" : [ 0, 1 ],
    "error" : null
  }, {
    "formattedValue" : "10759.0",
    "value" : 10759.0,
    "ordinal" : 2,
    "coordinates" : [ 0, 2 ],
    "error" : null
  }, {
    "formattedValue" : "10589.0",
    "value" : 10589.0,
    "ordinal" : 3,
    "coordinates" : [ 0, 3 ],
    "error" : null
  }, {
    "formattedValue" : "5904.0",
    "value" : 5904.0,
    "ordinal" : 4,
    "coordinates" : [ 0, 4 ],
    "error" : null
  }, {
    "formattedValue" : "115670.0",
    "value" : 115670.0,
    "ordinal" : 5,
    "coordinates" : [ 0, 5 ],
    "error" : null
  } ],
  "axes" : [ {
    "ordinal" : 0,
    "name" : "COLUMNS",
    "positions" : [ {
      "memberDimensionNames" : [ "Measures" ],
      "memberDimensionCaptions" : [ "Measures" ],
      "memberDimensionValues" : [ "Units Shipped" ]
    } ]
  }, {
    "ordinal" : 1,
    "name" : "ROWS",
    "positions" : [ {
      "memberDimensionNames" : [ "Store" ],
      "memberDimensionCaptions" : [ "Store" ],
      "memberDimensionValues" : [ "All Store Types" ]
    }, {
      "memberDimensionNames" : [ "Store" ],
      "memberDimensionCaptions" : [ "Store" ],
      "memberDimensionValues" : [ "Deluxe Supermarket" ]
    }, {
      "memberDimensionNames" : [ "Store" ],
      "memberDimensionCaptions" : [ "Store" ],
      "memberDimensionValues" : [ "Gourmet Supermarket" ]
    }, {
      "memberDimensionNames" : [ "Store" ],
      "memberDimensionCaptions" : [ "Store" ],
      "memberDimensionValues" : [ "Mid-Size Grocery" ]
    }, {
      "memberDimensionNames" : [ "Store" ],
      "memberDimensionCaptions" : [ "Store" ],
      "memberDimensionValues" : [ "Small Grocery" ]
    }, {
      "memberDimensionNames" : [ "Store" ],
      "memberDimensionCaptions" : [ "Store" ],
      "memberDimensionValues" : [ "Supermarket" ]
    } ]
  } ]
}
$:
```

You can also include a boolean property, named `tidy`, in the query request.  This causes the service to return more "tabularized" data, suitable for loading into a data frame or using in a visualization tool like Vega-lite.  Example:

```
curl -s -X POST -H 'Content-Type: application/json' http://localhost:58080/mondrian-rest/query -d '{"connectionName" : "foodmart", "query" : "select { [Measures].[Units Shipped] } on columns, NON EMPTY [Store].[Store Type].members on rows from Warehouse", "tidy" : true }'
[ {
  "CellValue" : 207726.0,
  "[Measures].[MeasuresLevel]" : "Units Shipped"
}, {
  "CellValue" : 64804.0,
  "[Measures].[MeasuresLevel]" : "Units Shipped",
  "[Store].[Store Type].[Store Type]" : "Deluxe Supermarket"
}, {
  "CellValue" : 10759.0,
  "[Measures].[MeasuresLevel]" : "Units Shipped",
  "[Store].[Store Type].[Store Type]" : "Gourmet Supermarket"
}, {
  "CellValue" : 10589.0,
  "[Measures].[MeasuresLevel]" : "Units Shipped",
  "[Store].[Store Type].[Store Type]" : "Mid-Size Grocery"
}, {
  "CellValue" : 5904.0,
  "[Measures].[MeasuresLevel]" : "Units Shipped",
  "[Store].[Store Type].[Store Type]" : "Small Grocery"
}, {
  "CellValue" : 115670.0,
  "[Measures].[MeasuresLevel]" : "Units Shipped",
  "[Store].[Store Type].[Store Type]" : "Supermarket"
} ]
$:
```

#### Building from source

To build the application with Maven, just go into the root directory (where the pom.xml is) and run:

`mvn install`

It will produce files `mondrian-rest.war` and `mondrian-rest-executable.war` in the `target` directory.

If you don't want to run the API from Docker, just drop `mondrian-rest.war` into your favorite J2EE servlet container (e.g., Tomcat's `webapps` directory).

You can also run the web application as a Spring Boot standalone application, with embedded Tomcat.  From the root directory (where pom.xml is), run `mondrian-rest-executable.war` like this:

```
$: java -jar target/mondrian-rest-executable.war

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.5.6.RELEASE)

[      main] org.ojbc.mondrian.rest.Application       INFO  Starting Application v0.0.1-SNAPSHOT on Scotts-MacBook-Pro.local with PID 85195 (/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war started by scott in /Users/scott/git-repos/ojbc/mondrian-rest)
[      main] org.ojbc.mondrian.rest.Application       INFO  No active profile set, falling back to default profiles: default
[      main] ationConfigEmbeddedWebApplicationContext INFO  Refreshing org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@5fa7e7ff: startup date [Fri Aug 04 16:55:10 PDT 2017]; root of context hierarchy
[nd-preinit] ibernate.validator.internal.util.Version INFO  HV000001: Hibernate Validator 5.3.5.Final
[      main] ed.tomcat.TomcatEmbeddedServletContainer INFO  Tomcat initialized with port(s): 8080 (http)
[      main] org.apache.catalina.core.StandardService INFO  Starting service [Tomcat]
[      main] org.apache.catalina.core.StandardEngine  INFO  Starting Servlet Engine: Apache Tomcat/8.5.16
[tartStop-1] org.apache.jasper.servlet.TldScanner     INFO  At least one JAR was scanned for TLDs yet contained no TLDs. Enable debug logging for this logger for a complete list of JARs that were scanned but no TLDs were found in them. Skipping unneeded JARs during scanning can improve startup time and JSP compilation time.
[tartStop-1] e.ContainerBase.[Tomcat].[localhost].[/] INFO  Initializing Spring embedded WebApplicationContext
[tartStop-1] pringframework.web.context.ContextLoader INFO  Root WebApplicationContext: initialization completed in 2785 ms
[tartStop-1] boot.web.servlet.ServletRegistrationBean INFO  Mapping servlet: 'dispatcherServlet' to [/]
[tartStop-1] .boot.web.servlet.FilterRegistrationBean INFO  Mapping filter: 'characterEncodingFilter' to: [/*]
[tartStop-1] .boot.web.servlet.FilterRegistrationBean INFO  Mapping filter: 'hiddenHttpMethodFilter' to: [/*]
[tartStop-1] .boot.web.servlet.FilterRegistrationBean INFO  Mapping filter: 'httpPutFormContentFilter' to: [/*]
[tartStop-1] .boot.web.servlet.FilterRegistrationBean INFO  Mapping filter: 'requestContextFilter' to: [/*]
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Processing connection definition json found at jar:file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes/mondrian-connections.json
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Adding valid connection test: connection string=jdbc:hsqldb:res:test, Mondrian schema path=jar:file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes!/test.xml
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Adding valid connection foodmart: connection string=jdbc:hsqldb:res:foodmart;set schema "foodmart", Mondrian schema path=https://raw.githubusercontent.com/pentaho/mondrian/lagunitas/demo/FoodMart.mondrian.xml
[      main] .annotation.RequestMappingHandlerAdapter INFO  Looking for @ControllerAdvice: org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@5fa7e7ff: startup date [Fri Aug 04 16:55:10 PDT 2017]; root of context hierarchy
[      main] .annotation.RequestMappingHandlerMapping INFO  Mapped "{[/query],methods=[POST],consumes=[application/json],produces=[application/json]}" onto public org.springframework.http.ResponseEntity<java.lang.String> org.ojbc.mondrian.rest.MondrianRestController.query(org.ojbc.mondrian.rest.QueryRequest) throws java.lang.Exception
[      main] .annotation.RequestMappingHandlerMapping INFO  Mapped "{[/getConnections],methods=[GET],produces=[application/json]}" onto public java.lang.String org.ojbc.mondrian.rest.MondrianRestController.getConnections() throws java.lang.Exception
[      main] .annotation.RequestMappingHandlerMapping INFO  Mapped "{[/getSchema],methods=[GET],produces=[application/xml]}" onto public org.springframework.http.ResponseEntity<java.lang.String> org.ojbc.mondrian.rest.MondrianRestController.getSchema(java.lang.String) throws java.lang.Exception
[      main] .annotation.RequestMappingHandlerMapping INFO  Mapped "{[/error]}" onto public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)
[      main] .annotation.RequestMappingHandlerMapping INFO  Mapped "{[/error],produces=[text/html]}" onto public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)
[      main] .servlet.handler.SimpleUrlHandlerMapping INFO  Mapped URL path [/webjars/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
[      main] .servlet.handler.SimpleUrlHandlerMapping INFO  Mapped URL path [/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
[      main] .servlet.handler.SimpleUrlHandlerMapping INFO  Mapped URL path [/**/favicon.ico] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
[      main] xport.annotation.AnnotationMBeanExporter INFO  Registering beans for JMX exposure on startup
[      main] g.apache.coyote.http11.Http11NioProtocol INFO  Initializing ProtocolHandler ["http-nio-8080"]
[      main] g.apache.coyote.http11.Http11NioProtocol INFO  Starting ProtocolHandler ["http-nio-8080"]
[      main] g.apache.tomcat.util.net.NioSelectorPool INFO  Using a shared selector for servlet write/read
[      main] ed.tomcat.TomcatEmbeddedServletContainer INFO  Tomcat started on port(s): 8080 (http)
[      main] org.ojbc.mondrian.rest.Application       INFO  Started Application in 5.02 seconds (JVM running for 5.613)
```

Then the API will be available at http://localhost:8080/...


