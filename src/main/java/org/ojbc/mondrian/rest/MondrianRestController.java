/*
 * Unless explicitly acquired and licensed from Licensor under another license, the contents of
 * this file are subject to the Reciprocal Public License ("RPL") Version 1.5, or subsequent
 * versions as allowed by the RPL, and You may not copy or use this file in either source code
 * or executable form, except in compliance with the terms and conditions of the RPL
 *
 * All software distributed under the RPL is provided strictly on an "AS IS" basis, WITHOUT
 * WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND LICENSOR HEREBY DISCLAIMS ALL SUCH
 * WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RPL for specific language
 * governing rights and limitations under the RPL.
 *
 * http://opensource.org/licenses/RPL-1.5
 *
 * Copyright 2012-2017 Open Justice Broker Consortium
 */
package org.ojbc.mondrian.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ojbc.mondrian.CellSetWrapper;
import org.ojbc.mondrian.MondrianConnectionFactory;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST API for interacting with Mondrian.
 *
 */
@RestController
public class MondrianRestController {
	
	private final Log log = LogFactory.getLog(MondrianRestController.class);
	private MondrianConnectionFactory connectionFactory;
	
	public MondrianRestController() throws IOException {
		connectionFactory = new MondrianConnectionFactory();
		connectionFactory.init();
	}
	
	/**
	 * Get all the connections available to this instance of the API
	 * @return json string with connection information
	 * @throws Exception
	 */
	@RequestMapping(value="/getConnections", method=RequestMethod.GET, produces="application/json")
	public String getConnections() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.addMixIn(MondrianConnectionFactory.MondrianConnection.class, SchemaContentHidingMixIn.class);
		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(connectionFactory.getConnections());
	}
	
	/**
	 * Get the Mondrian schema XML for the specified connection.  Sets HTTP Status of 500 if the specified connection does not exist.
	 * @param connectionName the connection to search for
	 * @return the specified connection's Mondrian schema (as XML), or null if not found
	 * @throws Exception
	 */
	@RequestMapping(value="/getSchema", method=RequestMethod.GET, produces="application/xml")
	public ResponseEntity<String> getSchema(String connectionName) throws Exception {
		
		String body = null;
		HttpStatus status = HttpStatus.OK;
		
		MondrianConnectionFactory.MondrianConnection connection = connectionFactory.getConnections().get(connectionName);
		if (connection == null) {
			log.warn("Attempt to retrieve schema for connection that does not exist: " + connectionName);
			status = HttpStatus.NOT_FOUND;
		} else {
			log.info("Retrieving schema content for connection " + connectionName);
			String schemaContent = connection.getMondrianSchemaContent();
			body = schemaContent.replace("\\n", "\n");
		}
		
		return new ResponseEntity<String>(body, status);
		
	}
	
	/**
	 * Submit the specified MDX query to the specified Mondrian connection.  Sets HTTP Status of 500 if the specified connection does not exist or if the query syntax is invalid.
	 * @param queryRequest the query request (specifies the connection, by name, and the MDX query string)
	 * @return json string containing the resulting CellSet, or null if no results
	 * @throws Exception
	 */
	@RequestMapping(value="/query", method=RequestMethod.POST, produces="application/json", consumes="application/json")
	public ResponseEntity<String> query(@RequestBody QueryRequest queryRequest) throws Exception {
		
		String body = null;
		HttpStatus status = HttpStatus.OK;
		
		String connectionName = queryRequest.getConnectionName();
		MondrianConnectionFactory.MondrianConnection connection = connectionFactory.getConnections().get(connectionName);
		
		if (connection == null) {
			log.warn("Query submitted for connection that does not exist: " + connectionName);
			status = HttpStatus.NOT_FOUND;
		} else {
			OlapConnection olapConnection = connection.getOlap4jConnection().unwrap(OlapConnection.class);
			OlapStatement statement = olapConnection.createStatement();
			String query = queryRequest.getQuery();
			log.info("Executing query on connection " + connectionName + ": " + query);
			ObjectMapper mapper = new ObjectMapper();
			try {
				CellSet cellSet = statement.executeOlapQuery(query);
				CellSetWrapper w = new CellSetWrapper(cellSet);
				body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(w);
			} catch (OlapException oe) {
				log.warn("OlapException occurred processing query.  Stack trace follows.");
				oe.printStackTrace();
				Map<String, String> errorBodyMap = new HashMap<>();
				errorBodyMap.put("reason", oe.getMessage());
				Throwable rootCause = oe;
				Throwable nextCause = oe.getCause();
				while (nextCause != null) {
					rootCause = nextCause;
					nextCause = rootCause.getCause();
				}
				errorBodyMap.put("rootCauseReason", rootCause.getMessage());
				errorBodyMap.put("SQLState", oe.getSQLState());
				body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorBodyMap);
				status = HttpStatus.valueOf(500);
			}
			
		}
		
		return new ResponseEntity<String>(body, status);
		
	}
	
	static final class SchemaContentHidingMixIn {
		@JsonIgnore
		@JsonProperty("MondrianSchemaContent")
		public String getMondrianSchemaContent() {
			return null;
		}
	}

}
