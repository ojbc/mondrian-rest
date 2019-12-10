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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.ojbc.mondrian.CellSetWrapper;
import org.ojbc.mondrian.CellSetWrapperType;
import org.ojbc.mondrian.MondrianConnectionFactory;
import org.ojbc.mondrian.SchemaWrapper;
import org.ojbc.mondrian.TidyCellSetWrapper;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.metadata.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
	private Cache<Integer, CellSetWrapperType> queryCache;
	
   	@Resource(name="${requestAuthorizerBeanName}")
	private RequestAuthorizer requestAuthorizer;
   	
	@Value("${removeDemoConnections}")
	private boolean removeDemoConnections;
	
	@PostConstruct
	public void init() throws IOException {
		connectionFactory = new MondrianConnectionFactory();
		connectionFactory.init(removeDemoConnections);
		Configuration cacheConfig = new XmlConfiguration(getClass().getResource("/ehcache-config.xml")); 
		CacheManager cacheManager = CacheManagerBuilder.newCacheManager(cacheConfig);
		cacheManager.init();
		queryCache = cacheManager.getCache("query-cache", Integer.class, CellSetWrapperType.class);
		log.info("Successfully registered request authorizer class "
				+ requestAuthorizer.getClass().getName());
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
	 * Flush the query cache
	 */
	@RequestMapping(value="/flushCache", method=RequestMethod.GET)
	public ResponseEntity<Void> flushCache() {
		queryCache.clear();
		log.info("Query cache flushed");
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@RequestMapping(value="/getMetadata", method=RequestMethod.GET, produces="application/json")
	public ResponseEntity<String> getMetadata(String connectionName) throws Exception {
		
		String body = null;
		HttpStatus status = HttpStatus.OK;
		
		MondrianConnectionFactory.MondrianConnection connection = connectionFactory.getConnections().get(connectionName);
		
		if (connection == null) {
			log.warn("Attempt to retrieve metadata for connection that does not exist: " + connectionName);
			status = HttpStatus.NOT_FOUND;
		} else {
			ObjectMapper mapper = new ObjectMapper();
			try {
				
				OlapConnection olapConnection = connection.getOlap4jConnection().unwrap(OlapConnection.class);
				Schema schema = olapConnection.getOlapSchema();
				SchemaWrapper schemaWrapper = new SchemaWrapper(schema, connectionName);
				body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaWrapper);
			} catch (OlapException oe) {
				log.warn("OlapException occurred retrieving metadata.  Stack trace follows (if debug logging).");
				log.debug("Stack trace: ", oe);
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
				log.warn("Exception root cause: " + rootCause.getMessage());
				body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorBodyMap);
				status = HttpStatus.valueOf(500);
			}
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
	public ResponseEntity<String> query(@RequestBody QueryRequest queryRequest, HttpServletRequest request) throws Exception {
		
		RequestAuthorizer.RequestAuthorizationStatus authorizationStatus = requestAuthorizer.authorizeRequest(request, queryRequest);
		
		if (authorizationStatus.authorized) {

			boolean tidy = false;
			boolean simplifyNames = false;
			String body = null;
			HttpStatus status = HttpStatus.OK;
			HttpHeaders responseHeaders = new HttpHeaders();

			String connectionName = queryRequest.getConnectionName();

			Map<String, String> levelNameTranslationMap = null;
			QueryRequest.TidyConfig tidyConfig = queryRequest.getTidy();
			if (tidyConfig != null) {
				tidy = tidyConfig.isEnabled();
				simplifyNames = tidyConfig.isSimplifyNames();
				levelNameTranslationMap = tidyConfig.getLevelNameTranslationMap();
				if (simplifyNames && !tidy) {
					log.warn(
							"Request for simplification of names, but tidy is false.  No simplification is performed on raw CellSetWrappers.");
				}
			}

			MondrianConnectionFactory.MondrianConnection connection = connectionFactory.getConnections()
					.get(connectionName);
			ObjectMapper mapper = new ObjectMapper();

			if (connection == null) {

				String missingConnectionErrorMessage = "Query submitted for connection that does not exist: " + connectionName;
				log.warn(missingConnectionErrorMessage);
				status = HttpStatus.NOT_FOUND;
				body = "{\"message\" : \"" + missingConnectionErrorMessage + "\"}";

			} else {

				String query = queryRequest.getQuery();
				String mondrianRoleName = authorizationStatus.mondrianRole;
				queryRequest.setMondrianRole(mondrianRoleName);

				log.info("Token " + authorizationStatus.token + " with role " + mondrianRoleName +
						" executing query on connection " + connectionName + " with tidy=" + tidy + ": " + query);

				CellSetWrapperType outputObject = null;
				boolean querySucceeded = false;

				int cacheKey = queryRequest.getCacheKey();

				if (queryCache.containsKey(cacheKey)) {

					outputObject = queryCache.get(cacheKey);
					responseHeaders.add("mondrian-rest-cached-result", "true");
					log.info("Retrieved query result from cache");
					querySucceeded = true;

				} else {

					OlapConnection olapConnection = connection.getOlap4jConnection().unwrap(OlapConnection.class);
					try {
						if (mondrianRoleName != null) {
							olapConnection.setRoleName(mondrianRoleName);
						}
						OlapStatement statement = olapConnection.createStatement();
						try {
							CellSet cellSet = statement.executeOlapQuery(query);
							log.debug("Query succeeded");
							if (tidy) {
								TidyCellSetWrapper tcc = new TidyCellSetWrapper();
								tcc.init(cellSet, simplifyNames, levelNameTranslationMap);
								outputObject = tcc;
							} else {
								outputObject = new CellSetWrapper(cellSet);
							}
							queryCache.put(cacheKey, outputObject);
							querySucceeded = true;
						} catch (OlapException oe) {
							log.warn("OlapException occurred processing query.  Stack trace follows (if debug logging).");
							log.debug("Stack trace: ", oe);
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
							log.warn("Exception root cause: " + rootCause.getMessage());
							body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorBodyMap);
							status = HttpStatus.valueOf(500);
						} finally {
							statement.close();
						}
					} finally {
						olapConnection.close();
					}

				}

				if (querySucceeded) {
					body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outputObject);
				}

			}

			return new ResponseEntity<String>(body, responseHeaders, status);

		} else {
			
			log.warn(authorizationStatus.message);
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			
		}
		
	}
	
	static final class SchemaContentHidingMixIn {
		@JsonIgnore
		@JsonProperty("MondrianSchemaContent")
		public String getMondrianSchemaContent() {
			return null;
		}
	}

	public void setRemoveDemoConnections(boolean removeDemoConnections) {
		this.removeDemoConnections = removeDemoConnections;
	}

}
