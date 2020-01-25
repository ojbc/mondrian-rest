Overview
--------

This repository contains a [Spring Boot](https://projects.spring.io/spring-boot/) -based REST API for querying relational [OLAP](https://en.wikipedia.org/wiki/Online_analytical_processing) data sources via [Mondrian](http://community.pentaho.com/projects/mondrian/).

### Table of Contents

* [Motivation](#motivation)
* [Mondrian versions](#mondrian-versions)
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

### Mondrian versions

Initial development of mondrian-rest supported version 4.x of the Mondrian library. However, Mondrian 4.x does not appear to be under active development by the core Mondrian maintainers, so we have abandoned it as a dependency. The last version of mondrian-rest
that supports version 4.x schemas is [version 1.5.0](https://github.com/ojbc/mondrian-rest/tree/v1.5.0). PRs for the 1.x line of mondrian-rest will be considered, but active development of mondrian-rest by the core committers is focused on Mondrian 8.x (and, at some point, version
9.x). There are significant schema differences between 4.x and 8.x+, however for the most part, the mondrian-rest API is consistent between mondrian-rest v2.x and v1.x.

### General Usage

You can run the API in [Docker](https://www.docker.com/) or in an existing J2EE servlet container.  Instructions for each option appear below.

The API web application includes the Mondrian FoodMart demonstration database and schema, and a small database and schema that we use for testing.  By default, these connections will be available when the API application is started. To turn them
off, set an application property `removeDemoConnections` to `true`.  Instructions for accomplishing this appear in the Docker and J2EE container sections below.

#### Configuration

Configuration of the controller occurs via editing of the Spring [`application.properties`](https://github.com/ojbc/mondrian-rest/blob/master/src/main/resources/application.properties) file. The file contains documentation for each of the available
properties. To configure an instance of the controller, edit the file and place it on Tomcat's classpath (typically this is accomplished by enabling Tomcat's shared loader to point to a directory, and then placing the edited properties file in that directory).
If you run the application directly via the executable war file, you can set any of the available properties with Java system properties as well.

Configuration allows you to control the following behavior of the controller:

* User authentication and mapping of users to Mondrian roles (for enabling schema security)
* Whether the included demo connections / data sources are enabled
* Whether the simple query UI is enabled
* Pre-caching of metadata (to trade off longer controller startup time for quicker response to the first `/query` or `/getMetadata` call)
* Setting a timeout for queries

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
 :: Spring Boot ::        (v2.2.2.RELEASE)

[      main] org.ojbc.mondrian.rest.Application       INFO  Starting Application v2.0.1 on smc-mbp.local with PID 73198 (/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war started by scott in /Users/scott/git-repos/ojbc/mondrian-rest)
[      main] org.ojbc.mondrian.rest.Application       INFO  No active profile set, falling back to default profiles: default
[      main] boot.web.embedded.tomcat.TomcatWebServer INFO  Tomcat initialized with port(s): 8080 (http)
[      main] g.apache.coyote.http11.Http11NioProtocol INFO  Initializing ProtocolHandler ["http-nio-8080"]
[      main] org.apache.catalina.core.StandardService INFO  Starting service [Tomcat]
[      main] org.apache.catalina.core.StandardEngine  INFO  Starting Servlet engine: [Apache Tomcat/9.0.29]
[      main] e.ContainerBase.[Tomcat].[localhost].[/] INFO  Initializing Spring embedded WebApplicationContext
[      main] pringframework.web.context.ContextLoader INFO  Root WebApplicationContext: initialization completed in 2443 ms
[      main] mondrian.olap.MondrianProperties         INFO  Mondrian: properties loaded from 'jar:file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes!/mondrian.properties'
[      main] mondrian.olap.MondrianProperties         INFO  Mondrian: loaded 0 system properties
[      main] jbc.mondrian.rest.MondrianRestController INFO  Initializing controller, Mondrian version is: 8.0
[      main] jbc.mondrian.rest.MondrianRestController INFO  No query timeout specified
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Working around Spring Boot / Tomcat bug that occurs in standalone mode, to adjust file path found via PathMatchingResourcePatternResolver
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Processing connection definition json found at jar:file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes/mondrian-connections.json
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Adding valid connection test: connection string=jdbc:hsqldb:res:test, Mondrian schema path=jar:file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes!/test.xml
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Adding valid connection foodmart: connection string=jdbc:hsqldb:res:foodmart;set schema "foodmart", Mondrian schema path=https://raw.githubusercontent.com/pentaho/mondrian/master/demo/FoodMart.xml
[      main] org.ehcache.core.EhcacheManager          INFO  Cache 'metadata-cache' created in EhcacheManager.
[      main] org.ehcache.core.EhcacheManager          INFO  Cache 'query-cache' created in EhcacheManager.
[      main] jbc.mondrian.rest.MondrianRestController INFO  Successfully registered request authorizer class org.ojbc.mondrian.rest.DefaultRequestAuthorizer
[      main] duling.concurrent.ThreadPoolTaskExecutor INFO  Initializing ExecutorService 'applicationTaskExecutor'
[      main] g.apache.coyote.http11.Http11NioProtocol INFO  Starting ProtocolHandler ["http-nio-8080"]
[      main] boot.web.embedded.tomcat.TomcatWebServer INFO  Tomcat started on port(s): 8080 (http) with context path ''
[      main] org.ojbc.mondrian.rest.Application       INFO  Started Application in 5.702 seconds (JVM running for 6.699)
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
<?xml version="1.0"?>
<Schema name="FoodMart">
<!--
  == This software is subject to the terms of the Eclipse Public License v1.0
  == Agreement, available at the following URL:
  == http://www.eclipse.org/legal/epl-v10.html.
  == You must accept the terms of that agreement to use this software.
  ==
  == Copyright (C) 2000-2005 Julian Hyde
  == Copyright (C) 2005-2019 Hitachi Vantara and others
  == All Rights Reserved.
  -->

<!-- Shared dimensions -->

  <Dimension name="Store">
    <Hierarchy hasAll="true" primaryKey="store_id">
      <Table name="store"/>
      <Level name="Store Country" column="store_country" uniqueMembers="true"/>
      <Level name="Store State" column="store_state" uniqueMembers="true"/>
      <Level name="Store City" column="store_city" uniqueMembers="false"/>
      <Level name="Store Name" column="store_name" uniqueMembers="true">
        <Property name="Store Type" column="store_type"/>
        <Property name="Store Manager" column="store_manager"/>
        <Property name="Store Sqft" column="store_sqft" type="Numeric"/>
        <Property name="Grocery Sqft" column="grocery_sqft" type="Numeric"/>
        <Property name="Frozen Sqft" column="frozen_sqft" type="Numeric"/>
        <Property name="Meat Sqft" column="meat_sqft" type="Numeric"/>
        <Property name="Has coffee bar" column="coffee_bar" type="Boolean"/>
        <Property name="Street address" column="store_street_address" type="String"/>
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
  "connectionName" : "foodmart",
  "cubes" : [ {
    "name" : "Warehouse",
    "caption" : "Warehouse",
    "measures" : [ {
      "name" : "Store Invoice",
      "caption" : "Store Invoice",
      "visible" : true,
      "calculated" : false
    }, {
      "name" : "Supply Time",
      "caption" : "Supply Time",
      "visible" : true,
      "calculated" : false
    }, {
      "name" : "Warehouse Cost",
      "caption" : "Warehouse Cost",
      "visible" : true,
      "calculated" : false
    }, {
      "name" : "Warehouse Sales",
      "caption" : "Warehouse Sales",
      "visible" : true,
      "calculated" : false
    }, {
      "name" : "Units Shipped",
      "caption" : "Units Shipped",
      "visible" : true,
      "calculated" : false
    }, {
      "name" : "Units Ordered",
      "caption" : "Units Ordered",
      "visible" : true,
      "calculated" : false
    }, {
      "name" : "Warehouse Profit",
      "caption" : "Warehouse Profit",
      "visible" : true,
      "calculated" : false
    }, {
      "name" : "Fact Count",
      "caption" : "Fact Count",
      "visible" : false,
      "calculated" : false
    }, {
      "name" : "Average Warehouse Sale",
      "caption" : "Average Warehouse Sale",
      "visible" : true,
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
 :: Spring Boot ::        (v2.2.2.RELEASE)

[      main] org.ojbc.mondrian.rest.Application       INFO  Starting Application v2.0.1 on smc-mbp.local with PID 78583 (/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war started by scott in /Users/scott/git-repos/ojbc/mondrian-rest)
[      main] org.ojbc.mondrian.rest.Application       INFO  No active profile set, falling back to default profiles: default
[      main] boot.web.embedded.tomcat.TomcatWebServer INFO  Tomcat initialized with port(s): 8080 (http)
[      main] g.apache.coyote.http11.Http11NioProtocol INFO  Initializing ProtocolHandler ["http-nio-8080"]
[      main] org.apache.catalina.core.StandardService INFO  Starting service [Tomcat]
[      main] org.apache.catalina.core.StandardEngine  INFO  Starting Servlet engine: [Apache Tomcat/9.0.29]
[      main] e.ContainerBase.[Tomcat].[localhost].[/] INFO  Initializing Spring embedded WebApplicationContext
[      main] pringframework.web.context.ContextLoader INFO  Root WebApplicationContext: initialization completed in 2076 ms
[      main] mondrian.olap.MondrianProperties         INFO  Mondrian: properties loaded from 'jar:file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes!/mondrian.properties'
[      main] mondrian.olap.MondrianProperties         INFO  Mondrian: loaded 0 system properties
[      main] jbc.mondrian.rest.MondrianRestController INFO  Initializing controller, Mondrian version is: 8.0
[      main] jbc.mondrian.rest.MondrianRestController INFO  No query timeout specified
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Working around Spring Boot / Tomcat bug that occurs in standalone mode, to adjust file path found via PathMatchingResourcePatternResolver
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Processing connection definition json found at jar:file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes/mondrian-connections.json
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Adding valid connection test: connection string=jdbc:hsqldb:res:test, Mondrian schema path=jar:file:/Users/scott/git-repos/ojbc/mondrian-rest/target/mondrian-rest-executable.war!/WEB-INF/classes!/test.xml
[      main] .ojbc.mondrian.MondrianConnectionFactory INFO  Adding valid connection foodmart: connection string=jdbc:hsqldb:res:foodmart;set schema "foodmart", Mondrian schema path=https://raw.githubusercontent.com/pentaho/mondrian/master/demo/FoodMart.xml
[      main] org.ehcache.core.EhcacheManager          INFO  Cache 'metadata-cache' created in EhcacheManager.
[      main] org.ehcache.core.EhcacheManager          INFO  Cache 'query-cache' created in EhcacheManager.
[      main] jbc.mondrian.rest.MondrianRestController INFO  Successfully registered request authorizer class org.ojbc.mondrian.rest.BearerTokenRequestAuthorizer << ***
[      main] duling.concurrent.ThreadPoolTaskExecutor INFO  Initializing ExecutorService 'applicationTaskExecutor'
[      main] g.apache.coyote.http11.Http11NioProtocol INFO  Starting ProtocolHandler ["http-nio-8080"]
[      main] boot.web.embedded.tomcat.TomcatWebServer INFO  Tomcat started on port(s): 8080 (http) with context path ''
[      main] org.ojbc.mondrian.rest.Application       INFO  Started Application in 4.703 seconds (JVM running for 5.287)

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
