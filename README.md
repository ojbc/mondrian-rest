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

#### Building from source

If you don't want to run the API from Docker, you can build it with Maven, which will generate a WAR file.  Just drop that WAR file into your favorite J2EE servlet container.

The Maven build is not currently set up to be able to run the application from an executable jar; fixing this is on the to-do list.

To build with Maven, just go into the root directory (where the .pom is) and run:

`mvn install`

It will produce a `mondrian-rest.war` file in the `target` directory.

