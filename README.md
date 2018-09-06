Overview
--------

This repository contains a [Spring Boot](https://projects.spring.io/spring-boot/) -based REST API for querying relational [OLAP](https://en.wikipedia.org/wiki/Online_analytical_processing) data sources via [Mondrian](http://community.pentaho.com/projects/mondrian/).

### Table of Contents

* [Motivation](#motivation)
* [General Usage](#general-usage)
* [Docker](#via-docker)
* [J2EE/Spring Boot](#via-j2ee-container-or-spring-boot)
* [API](#api-usage)
* [Authentication and Mondrian Security Integration](#authentication-and-mondrian-security-integration)
* [Tidying of Results](#tidying)
* [Caching](#caching)
* [Building from source](#building-from-source)

### Motivation

The Mondrian library is a terrific way to submit [MDX](https://en.wikipedia.org/wiki/MultiDimensional_eXpressions) queries to relational data sources.  Great interface platforms, like
[Saiku](http://community.meteorite.bi/) and [Pivot4J](http://www.pivot4j.org/) provide full-featured ad-hoc queries for end-users comfortable with [pivot tables](https://en.wikipedia.org/wiki/Pivot_table).  There are also toolkits like [Pentaho CTools](http://community.pentaho.com/ctools/) for developing dashboards.

What has been missing is a simple, basic, and clean REST API wrapped around the Mondrian library.  Each of the tools just mentioned uses its own approach for programmatic access to Mondrian, and does not really make that approach available (in an easy way) outside of that tool.  Thus, we decided to implement the mondrian-rest API.

### General Usage

You can run the API in [Docker](https://www.docker.com/) or in an existing J2EE servlet container.  Instructions for each option appear below.

The API web application includes the Mondrian FoodMart demonstration database and schema, and a small database and schema that we use for testing.  By default, these connections will be available when the API application is started. To turn them
off, set an application property `removeDemoConnections` to `true`.  Instructions for accomplishing this appear in the Docker and J2EE container sections below.

#### Via Docker

There are pre-built Docker images on DockerHub with the API installed, one with default (i.e., no) security, and one configured with Bearer Token authentication and role-mapping.  The unsecured image is `ojbc/mondrian-rest:no-auth` (which is also
  tagged as `ojbc/mondrian-rest:latest`), and the secured image is `ojbc/mondrian-rest:bearer-auth`.  (For more on authentication and role-mapping, see the [security](#authentication-and-mondrian-security-integration) section below.)

The images expose the Mondrian demo FoodMart database and schema, and a small database/schema used for testing.

To run it in a local Docker environment:

`docker run -d --name=mondrian-rest -p 58080:80 ojbc/mondrian-rest` (replace 58080 with whatever port you prefer)

The API will then be running in Docker, exposed on that port, within Apache Tomcat.

To deploy connections with your custom schema and database information (i.e., for a "real" scenario), do the following in a derived image Dockerfile:

1. Create a `*mondrian-connections.json` file with the appropriate connection information for your database and schema, and place this file on the Tomcat shared classpath directory at `/opt/tomcat/shared/`.
  You can see examples in the [codebase](https://github.com/ojbc/mondrian-rest/blob/master/src/main/resources/mondrian-connections.json). _Important:_ For the application to find them, the files must end in the string `mondrian-connections.json` and must
  be on the classpath.  So `/opt/tomcat/shared/my-application-mondrian-connections.json` will work, but `.../my-application.json` and `.../my-application-connections.json` will not. Note that the reference to the Mondrian schema in the connection config
  must be preceded by a forward slash, assuming that you place the schema in the shared Tomcat directory root per the next step.  Be sure to set the `IsDemo` property in the json structure to `false`, otherwise if you set `removeDemoConnections` to `true` via
  application properties, your connection will be removed.
2. Place your Mondrian schema in `/opt/tomcat/shared` and ensure it has the same name as the schema reference in your `*mondrian-connections.json` file addressed in the prior step.

We typically create a custom Docker bridge network, and deploy the database and API in separate containers.  This enables the API container to reference the database via DNS, by container name.

If you want to disable the demo data connections, in your derived image Dockerfile, create an `application.properties` file with a line `removeDemoConnections=true` in it, and `COPY` this file to `/opt/tomcat/shared/config`.

#### Via J2EE Container or Spring Boot

If you don't want to run the API from Docker, just drop `mondrian-rest.war` (see [Building from Source](#building-from-source) below) into your favorite J2EE servlet container (e.g., Tomcat's `webapps` directory).

To remove demo connections and deploy your custom connections, follow instructions similar to those in the Docker section above as appropriate for your J2EE Servlet container.  For Tomcat, you can establish a directory as a "shared classpath" directory by
modifying `catalina.properties` and setting the `shared.loader` property to the desired location.

You can also run the web application as a Spring Boot standalone application, with embedded Tomcat.  From the root directory (where pom.xml is), run `mondrian-rest-executable.war` like this:

```
$ java -jar target/mondrian-rest-executable.war

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.5.6.RELEASE)

[      main] org.ojbc.mondrian.rest.Application       INFO  Starting Application v1.1.0 on smc-mbp.local with PID 5150 (/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war started by scott in /Users/scott/git-repos/ojbc/mondrian-rest)
[      main] org.ojbc.mondrian.rest.Application       INFO  No active profile set, falling back to default profiles: default
[      main] ationConfigEmbeddedWebApplicationContext INFO  Refreshing org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@28c97a5: startup date [Tue Feb 13 12:08:46 PST 2018]; root of context hierarchy
[nd-preinit] ibernate.validator.internal.util.Version INFO  HV000001: Hibernate Validator 5.3.5.Final
[      main] ed.tomcat.TomcatEmbeddedServletContainer INFO  Tomcat initialized with port(s): 8080 (http)
[      main] org.apache.catalina.core.StandardService INFO  Starting service [Tomcat]
[      main] org.apache.catalina.core.StandardEngine  INFO  Starting Servlet Engine: Apache Tomcat/8.5.16
[tartStop-1] org.apache.jasper.servlet.TldScanner     INFO  At least one JAR was scanned for TLDs yet contained no TLDs. Enable debug logging for this logger for a complete list of JARs that were scanned but no TLDs were found in them. Skipping unneeded JARs during scanning can improve startup time and JSP compilation time.
[tartStop-1] e.ContainerBase.[Tomcat].[localhost].[/] INFO  Initializing Spring embedded WebApplicationContext
[tartStop-1] pringframework.web.context.ContextLoader INFO  Root WebApplicationContext: initialization completed in 2601 ms
[tartStop-1] boot.web.servlet.ServletRegistrationBean INFO  Mapping servlet: 'dispatcherServlet' to [/]
[tartStop-1] .boot.web.servlet.FilterRegistrationBean INFO  Mapping filter: 'characterEncodingFilter' to: [/*]
[tartStop-1] .boot.web.servlet.FilterRegistrationBean INFO  Mapping filter: 'hiddenHttpMethodFilter' to: [/*]
[tartStop-1] .boot.web.servlet.FilterRegistrationBean INFO  Mapping filter: 'httpPutFormContentFilter' to: [/*]
[tartStop-1] .boot.web.servlet.FilterRegistrationBean INFO  Mapping filter: 'requestContextFilter' to: [/*]
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Working around Spring Boot / Tomcat bug that occurs in standalone mode, to adjust file path found via PathMatchingResourcePatternResolver
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Processing connection definition json found at jar:file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes/mondrian-connections.json
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Adding valid connection test: connection string=jdbc:hsqldb:res:test, Mondrian schema path=jar:file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes!/test.xml
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Adding valid connection foodmart: connection string=jdbc:hsqldb:res:foodmart;set schema "foodmart", Mondrian schema path=https://raw.githubusercontent.com/pentaho/mondrian/lagunitas/demo/FoodMart.mondrian.xml
[      main] org.ehcache.xml.XmlConfiguration         INFO  Loading Ehcache XML configuration from file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes!/ehcache-config.xml.
[      main] org.ehcache.core.EhcacheManager          INFO  Cache 'query-cache' created in EhcacheManager.
[      main] jbc.mondrian.rest.MondrianRestController INFO  Successfully registered request authorizer class org.ojbc.mondrian.rest.DefaultRequestAuthorizer
[      main] .annotation.RequestMappingHandlerAdapter INFO  Looking for @ControllerAdvice: org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@28c97a5: startup date [Tue Feb 13 12:08:46 PST 2018]; root of context hierarchy
[      main] .annotation.RequestMappingHandlerMapping INFO  Mapped "{[/query],methods=[POST],consumes=[application/json],produces=[application/json]}" onto public org.springframework.http.ResponseEntity<java.lang.String> org.ojbc.mondrian.rest.MondrianRestController.query(org.ojbc.mondrian.rest.QueryRequest,javax.servlet.http.HttpServletRequest) throws java.lang.Exception
[      main] .annotation.RequestMappingHandlerMapping INFO  Mapped "{[/getConnections],methods=[GET],produces=[application/json]}" onto public java.lang.String org.ojbc.mondrian.rest.MondrianRestController.getConnections() throws java.lang.Exception
[      main] .annotation.RequestMappingHandlerMapping INFO  Mapped "{[/flushCache],methods=[GET]}" onto public org.springframework.http.ResponseEntity<java.lang.Void> org.ojbc.mondrian.rest.MondrianRestController.flushCache()
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
[      main] org.ojbc.mondrian.rest.Application       INFO  Started Application in 6.122 seconds (JVM running for 6.796)
```

Then the API will be available at http://localhost:8080/... (e.g., http://localhost:8080/getConnections, http://localhost:8080/query, etc.)

If you want to disable the demo data connections, pass `removeDemoConnections=true` as a system property:

```
$ java -DremoveDemoConnections=true -jar target/mondrian-rest-executable.war
```

To load custom connections, run the executable warfile and pass a system property `shared.loader` as described [here](https://docs.spring.io/spring-boot/docs/current/reference/html/executable-jar.html#executable-jar-property-launcher-features), to add that
directory to the Spring Boot classpath.  Then place the `*mondrian-connections.json` file and Mondrian schema XML in that directory, similar to the manner described in the Docker section above.

#### API Usage

There are currently four main operations available in the API:

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

`/getMetadata?connectionName=[name]`: Retrieves the olap4j metadata structure for the specified connection

This contains much the same information as the `/getSchema` method.  However, `/getMetadata` actually retrieves the metadata
from a working connection, which enhances the returned information with data pulled from the underlying database source.  Importantly, this
includes the Members within each Level in each Dimension's Hierarchy.

Example:

```
$ curl -s http://localhost:8080/getMetadata?connectionName=foodmart | head -50
{
  "name" : "FoodMart",
  "cubes" : [ {
    "name" : "Sales 2",
    "caption" : "Sales 2",
    "measures" : [ {
      "name" : "Sales Count",
      "caption" : "Sales Count",
      "visible" : true,
      "calculated" : false
    }, {
      "name" : "Unit Sales",
      "caption" : "Unit Sales",
      "visible" : true,
      "calculated" : false
    }, {
      "name" : "Store Sales",
      "caption" : "Store Sales",
      "visible" : true,
      "calculated" : false
    }, {
      "name" : "Store Cost",
      "caption" : "Store Cost",
      "visible" : true,
      "calculated" : false
    }, {
      "name" : "Customer Count",
      "caption" : "Customer Count",
      "visible" : true,
      "calculated" : false
    }, {
      "name" : "Profit",
      "caption" : "Profit",
      "visible" : true,
      "calculated" : true
    }, {
      "name" : "Profit last Period",
      "caption" : "Profit last Period",
      "visible" : false,
      "calculated" : true
    } ],
    "dimensions" : [ {
      "name" : "Measures",
      "caption" : "Measures",
      "type" : "",
      "hierarchies" : [ {
        "name" : "Measures",
        "caption" : "Measures",
        "levels" : [ {
          "name" : "MeasuresLevel",
$:
```

`/query`: POST operation that takes an object with three properties:

* `connectionName` specifies the connection to query
* `query` specifies the MDX for the query
* `tidy` is a contained object to request "tidying" of the output, as explained below; the `tidy` object contains three properties:
	* `enabled` is a boolean to indicate whether to tidy the output
	* `simplifyNames` is a boolean to indicate whether to return simple names for dimension levels
	* `levelNameTranslationMap` is a map that associates level unique names with arbitrary simple names

Non-tidy example:

```
$: curl -s -X POST -H 'Content-Type: application/json' http://localhost:58080/mondrian-rest/query -d '{
>   "connectionName" : "foodmart",
>   "query" : "select { [Measures].[Units Shipped] } on columns, NON EMPTY [Store].[Store Type].members on rows from Warehouse"
> }'
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

### Authentication and Mondrian Security Integration

The API supports authentication of users and mapping of user credentials to roles defined in each connection's Mondrian schema.  Out of the box, the API supports Bearer Token authentication (and role-mapping by token), and
SAML Assertion authentication via Shibboleth (with role-mapping by an assertion attribute with the Name `gfipm:2.0:user:FederationId`).  Implementers can inject other authentication/role-mapping approaches via Spring (more details below).

To authenticate and map via Bearer Token authentication:

1. Run the API web application with an application property of `requestAuthorizerBeanName` set to the default bean name for the authorizer class.  The default bean name for the Bearer Token authorizer is `bearerTokenRequestAuthorizer` and for the
SAML assertion authorizer is `samlAssertionRequestAuthorizer`.  (To "run the application with an application property", see instructions in prior sections for setting the `removeDemoConnections` property.)
2. Place an appropriate authorizer configuration file on the classpath (again, following instructions for where to place `*mondrian-connections.json` files on the classpath under the different deployment scenarios in prior sections).  The configuration file
identifies the credentials that are authorized to use each connection, and maps each credential to a Mondrian schema role.  The format of this file will be specific to each authorizer implementation; consult the codebase for examples of
[Bearer Token](https://github.com/ojbc/mondrian-rest/blob/master/src/main/resources/bearer-token-request-authorizer.json) and [SAML Assertion](https://github.com/ojbc/mondrian-rest/blob/master/src/main/resources/saml-assertion-request-authorizer.json)
configurations.

The API application includes a Bearer Token authentication configuration designed to work with the two roles defined in the Mondrian demo FoodMart schema ("California Manager" and "No HR Cube" roles). To see this in action (using the Spring Boot
  deployment scenario here for illustration):

```
$ java -DrequestAuthorizerBeanName=bearerTokenRequestAuthorizer -jar target/mondrian-rest-executable.war

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.5.6.RELEASE)

[      main] org.ojbc.mondrian.rest.Application       INFO  Starting Application v1.1.0 on smc-mbp.local with PID 5263 (/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war started by scott in /Users/scott/git-repos/ojbc/mondrian-rest)
[      main] org.ojbc.mondrian.rest.Application       INFO  No active profile set, falling back to default profiles: default
[      main] ationConfigEmbeddedWebApplicationContext INFO  Refreshing org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@28c97a5: startup date [Tue Feb 13 13:17:01 PST 2018]; root of context hierarchy
[nd-preinit] ibernate.validator.internal.util.Version INFO  HV000001: Hibernate Validator 5.3.5.Final
[      main] ed.tomcat.TomcatEmbeddedServletContainer INFO  Tomcat initialized with port(s): 8080 (http)
[      main] org.apache.catalina.core.StandardService INFO  Starting service [Tomcat]
[      main] org.apache.catalina.core.StandardEngine  INFO  Starting Servlet Engine: Apache Tomcat/8.5.16
[tartStop-1] org.apache.jasper.servlet.TldScanner     INFO  At least one JAR was scanned for TLDs yet contained no TLDs. Enable debug logging for this logger for a complete list of JARs that were scanned but no TLDs were found in them. Skipping unneeded JARs during scanning can improve startup time and JSP compilation time.
[tartStop-1] e.ContainerBase.[Tomcat].[localhost].[/] INFO  Initializing Spring embedded WebApplicationContext
[tartStop-1] pringframework.web.context.ContextLoader INFO  Root WebApplicationContext: initialization completed in 2212 ms
[tartStop-1] boot.web.servlet.ServletRegistrationBean INFO  Mapping servlet: 'dispatcherServlet' to [/]
[tartStop-1] .boot.web.servlet.FilterRegistrationBean INFO  Mapping filter: 'characterEncodingFilter' to: [/*]
[tartStop-1] .boot.web.servlet.FilterRegistrationBean INFO  Mapping filter: 'hiddenHttpMethodFilter' to: [/*]
[tartStop-1] .boot.web.servlet.FilterRegistrationBean INFO  Mapping filter: 'httpPutFormContentFilter' to: [/*]
[tartStop-1] .boot.web.servlet.FilterRegistrationBean INFO  Mapping filter: 'requestContextFilter' to: [/*]
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Working around Spring Boot / Tomcat bug that occurs in standalone mode, to adjust file path found via PathMatchingResourcePatternResolver
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Processing connection definition json found at jar:file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes/mondrian-connections.json
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Adding valid connection test: connection string=jdbc:hsqldb:res:test, Mondrian schema path=jar:file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes!/test.xml
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Adding valid connection foodmart: connection string=jdbc:hsqldb:res:foodmart;set schema "foodmart", Mondrian schema path=https://raw.githubusercontent.com/pentaho/mondrian/lagunitas/demo/FoodMart.mondrian.xml
[      main] org.ehcache.xml.XmlConfiguration         INFO  Loading Ehcache XML configuration from file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes!/ehcache-config.xml.
[      main] org.ehcache.core.EhcacheManager          INFO  Cache 'query-cache' created in EhcacheManager.
[      main] jbc.mondrian.rest.MondrianRestController INFO  Successfully registered request authorizer class org.ojbc.mondrian.rest.BearerTokenRequestAuthorizer << ***
[      main] .annotation.RequestMappingHandlerAdapter INFO  Looking for @ControllerAdvice: org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@28c97a5: startup date [Tue Feb 13 13:17:01 PST 2018]; root of context hierarchy
[      main] .annotation.RequestMappingHandlerMapping INFO  Mapped "{[/query],methods=[POST],consumes=[application/json],produces=[application/json]}" onto public org.springframework.http.ResponseEntity<java.lang.String> org.ojbc.mondrian.rest.MondrianRestController.query(org.ojbc.mondrian.rest.QueryRequest,javax.servlet.http.HttpServletRequest) throws java.lang.Exception
[      main] .annotation.RequestMappingHandlerMapping INFO  Mapped "{[/getSchema],methods=[GET],produces=[application/xml]}" onto public org.springframework.http.ResponseEntity<java.lang.String> org.ojbc.mondrian.rest.MondrianRestController.getSchema(java.lang.String) throws java.lang.Exception
[      main] .annotation.RequestMappingHandlerMapping INFO  Mapped "{[/flushCache],methods=[GET]}" onto public org.springframework.http.ResponseEntity<java.lang.Void> org.ojbc.mondrian.rest.MondrianRestController.flushCache()
[      main] .annotation.RequestMappingHandlerMapping INFO  Mapped "{[/getConnections],methods=[GET],produces=[application/json]}" onto public java.lang.String org.ojbc.mondrian.rest.MondrianRestController.getConnections() throws java.lang.Exception
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
[      main] org.ojbc.mondrian.rest.Application       INFO  Started Application in 5.324 seconds (JVM running for 5.857)
```

Note the log message, indicated with `<< ***`, that tells us which request authorizer is being used.

Trying to access the API without a bearer token results in a 403 (forbidden):

```
$ curl -X POST   http://localhost:8080/query   -H 'Content-Type: application/json'   -d '{
> "connectionName": "foodmart",
> "query" : "select { [Measures].[Org Salary] } on columns, NON EMPTY [Employee].[Employees].children on rows from HR",
>     "tidy" : {
>       "enabled" : true,
>       "simplifyNames" : true
> }
> }' -i
HTTP/1.1 403
Content-Length: 0
Date: Tue, 13 Feb 2018 21:20:53 GMT
```

On the server side, we also see a message in the logs:

```
...
[080-exec-2] jbc.mondrian.rest.MondrianRestController WARN  Authentication failed, no bearer authentication header present in request.
...
```

Using an unrecognized token also results in a 403:

```
$ curl -X POST   http://localhost:8080/query   -H 'Content-Type: application/json' -H 'Authorization: Bearer UnrecognizedToken' -d '{
> "connectionName": "foodmart",
> "query" : "select { [Measures].[Org Salary] } on columns, NON EMPTY [Employee].[Employees].children on rows from HR",
>     "tidy" : {
>       "enabled" : true,
>       "simplifyNames" : true
> }
> }' -i
HTTP/1.1 403
Content-Length: 0
Date: Tue, 13 Feb 2018 21:20:53 GMT
```

But with a slightly different log message:

```
...
[080-exec-4] jbc.mondrian.rest.MondrianRestController WARN  Authentication failed.  Token UnrecognizedToken not found in config.
...
```

And if we authenticate with a token that is mapped to a Mondrian role that does not authorize access to the query, a 500 status results, with an error message sent back in the body:

```
$ curl -X POST \
>   http://localhost:8080/query \
>   -H 'Authorization: Bearer NO_HR_CUBE_TOKEN' \
>   -H 'Content-Type: application/json' \
>   -d '{
> "connectionName": "foodmart",
> "query" : "select { [Measures].[Org Salary] } on columns, NON EMPTY [Employee].[Employees].children on rows from HR",
>     "tidy" : {
>       "enabled" : true,
>       "simplifyNames" : true
> }
> }' -i
HTTP/1.1 500
Content-Type: application/json;charset=UTF-8
Content-Length: 147
Date: Tue, 13 Feb 2018 21:23:44 GMT
Connection: close

{
  "reason" : "mondrian gave exception while parsing query",
  "rootCauseReason" : "Mondrian Error:MDX cube 'HR' not found",
  "SQLState" : null
}
```

And the corresponding log message:

```
...
[080-exec-2] jbc.mondrian.rest.MondrianRestController INFO  Token NO_HR_CUBE_TOKEN with role No HR Cube executing query on connection foodmart with tidy=true: select { [Measures].[Org Salary] } on columns, NON EMPTY [Employee].[Employees].children on rows from HR
[080-exec-2] jbc.mondrian.rest.MondrianRestController WARN  OlapException occurred processing query.  Stack trace follows (if debug logging).
[080-exec-2] jbc.mondrian.rest.MondrianRestController WARN  Exception root cause: Mondrian Error:MDX cube 'HR' not found...
```

(Note that the token NO_HR_CUBE_TOKEN is mapped to the "No HR Cube" Mondrian role in the bearer token authentication configuration file [here](https://github.com/ojbc/mondrian-rest/blob/master/src/main/resources/bearer-token-request-authorizer.json)).

If we authenticate with a token that is mapped to a Mondrian role that _is_ authorized to access the query, we get a successful result:

```
$ curl -X POST \
>   http://localhost:8080/query \
>   -H 'Authorization: Bearer TOKEN1' \
>   -H 'Content-Type: application/json' \
>   -d '{
> "connectionName": "foodmart",
> "query" : "select { [Measures].[Org Salary] } on columns, NON EMPTY [Employee].[Employees].children on rows from HR",
>     "tidy" : {
>       "enabled" : true,
>       "simplifyNames" : true
> }
> }' -i
HTTP/1.1 200
mondrian-rest-cached-result: true
Content-Type: application/json;charset=UTF-8
Content-Length: 92
Date: Tue, 13 Feb 2018 21:29:19 GMT

{
  "values" : [ {
    "Employee Id" : "Sheri Nowmer",
    "Org Salary" : 39431.6712
  } ]
}
```

Note that there is a "special" Mondrian role, not defined in any schema, named `org.ojbc.mondrian.rest.RequestAuthorizer-All-Access`, that can be used to grant full access to a connection's schema.  This is essentially a way to authenticate a user, but
map that user to the default Mondrian role (which is no role), enabling full access.  In the example just above, the Bearer Token TOKEN1 is mapped to this role for the FoodMart connection, which is why the request is successful.

##### Implementing a custom request authorizer

It is straightforward to implement new request authorizers for new authentication schemes by implementing the `org.ojbc.mondrian.rest.RequestAuthorizer` interface, found
[here](https://github.com/ojbc/mondrian-rest/blob/master/src/main/java/org/ojbc/mondrian/rest/RequestAuthorizer.java).  This interface contains a single method, `authorizeRequest(HttpServletRequest request, QueryRequest queryRequest): RequestAuthorizationStatus`.
The bearer token and SAML assertion implementations should serve as good examples to follow.

#### Tidying

You can also include a property, named `tidy`, in the query request.  This causes the service to return more "tabularized" data, suitable for loading into a data frame or using in a visualization tool like
[Vega-lite](https://vega.github.io/vega-lite/).  Example:

```
$: curl -s -X POST -H 'Content-Type: application/json' http://localhost:58080/mondrian-rest/query -d '{
>   "connectionName" : "foodmart",
>   "query" : "select { [Measures].[Units Shipped] } on columns, NON EMPTY [Store].[Store Type].children on rows from Warehouse",
>   "tidy" : {
>     "enabled" : true
>   }
> }'
{
  "values" : [ {
    "[Store].[Store Type].[Store Type]" : "Deluxe Supermarket",
    "Units Shipped" : 64804.0
  }, {
    "[Store].[Store Type].[Store Type]" : "Gourmet Supermarket",
    "Units Shipped" : 10759.0
  }, {
    "[Store].[Store Type].[Store Type]" : "Mid-Size Grocery",
    "Units Shipped" : 10589.0
  }, {
    "[Store].[Store Type].[Store Type]" : "Small Grocery",
    "Units Shipped" : 5904.0
  }, {
    "[Store].[Store Type].[Store Type]" : "Supermarket",
    "Units Shipped" : 115670.0
  } ]
}
$:
```

Setting the `simplifyNames` property on the `tidy` object causes the service to remove the level's ancestors, and the brackets, from the level name:

```
$: curl -s -X POST -H 'Content-Type: application/json' http://localhost:58080/mondrian-rest/query -d '{
>    "connectionName" : "foodmart",
>    "query" : "select { [Measures].[Units Shipped] } on columns, NON EMPTY [Store].[Store Type].children on rows from Warehouse",
>    "tidy" : {
>      "enabled" : true,
>      "simplifyNames" : true
>    }
> }'
{
  "values" : [ {
    "Units Shipped" : 64804.0,
    "Store Type" : "Deluxe Supermarket"
  }, {
    "Units Shipped" : 10759.0,
    "Store Type" : "Gourmet Supermarket"
  }, {
    "Units Shipped" : 10589.0,
    "Store Type" : "Mid-Size Grocery"
  }, {
    "Units Shipped" : 5904.0,
    "Store Type" : "Small Grocery"
  }, {
    "Units Shipped" : 115670.0,
    "Store Type" : "Supermarket"
  } ]
}
$:
```

And by including a `levelNameTranslationMap` property, mapping level unique names to arbitrary names, it is possible to control the output even more:

```
$: curl -s -X POST -H 'Content-Type: application/json' http://localhost:58080/mondrian-rest/query -d '{
>     "connectionName" : "foodmart",
>     "query" : "select { [Measures].[Units Shipped] } on columns, NON EMPTY [Store].[Store Type].children on rows from Warehouse",
>     "tidy" : {
>       "enabled" : true,
>       "simplifyNames" : true,
>       "levelNameTranslationMap" : {
>         "[Store].[Store Type].[Store Type]" : "Kind of establishment"
>       }
>     }
> }'
{
  "values" : [ {
    "Units Shipped" : 64804.0,
    "Kind of establishment" : "Deluxe Supermarket"
  }, {
    "Units Shipped" : 10759.0,
    "Kind of establishment" : "Gourmet Supermarket"
  }, {
    "Units Shipped" : 10589.0,
    "Kind of establishment" : "Mid-Size Grocery"
  }, {
    "Units Shipped" : 5904.0,
    "Kind of establishment" : "Small Grocery"
  }, {
    "Units Shipped" : 115670.0,
    "Kind of establishment" : "Supermarket"
  } ]
}
$:
```

When specifying a `levelNameTranslationMap`, any unique names not found in the map are simplified as if no map were provided at all (i.e., by returning the child level's name only).

#### Caching

The service uses [ehCache](http://www.ehcache.org/) to provide on-heap caching of queries.  By default, 500 queries are cached.  To change caching behavior, place a file named
ehcache-config.xml on the classpath.

The `/flushCache` operation in the API will clear all cached queries from the cache.

#### Building from source

To build the application with Maven, just go into the root directory (where the pom.xml is) and run:

`mvn install`

It will produce files `mondrian-rest.war` and `mondrian-rest-executable.war` in the `target` directory.
