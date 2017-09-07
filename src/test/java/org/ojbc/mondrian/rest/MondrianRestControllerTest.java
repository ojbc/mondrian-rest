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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ojbc.mondrian.CellSetWrapper;
import org.ojbc.mondrian.MondrianConnectionFactory.MondrianConnection;
import org.ojbc.mondrian.TidyCellSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MondrianRestControllerTest {
	
	private final Log log = LogFactory.getLog(MondrianRestControllerTest.class);
	
	@LocalServerPort
	private String port;
	
	@Autowired
	private MondrianRestController controller;
	
	private HttpClient httpClient;
	
	@Before
    public void setUp() throws Exception {
		log.debug("setUp");
    	RequestConfig requestConfig = RequestConfig.custom().build();
    	HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    	clientBuilder.setDefaultRequestConfig(requestConfig);
    	httpClient = clientBuilder.build();
    	assertNotNull(httpClient);
    	assertNotNull(controller);
    	log.info("Randomly-assigned port is " + port);
    }
	
	@Test
	public void testGetConnections() throws Exception {
		
		HttpGet getRequest = new HttpGet("http://localhost:" + port + "/getConnections");
		getRequest.addHeader("accept", "application/json");
		HttpResponse response = httpClient.execute(getRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		String content = getBodyContent(response);
		
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<Map<String, MondrianConnection>> typeRef = new TypeReference<Map<String, MondrianConnection>>() {};
		Map<String, MondrianConnection> connections = mapper.readValue(content, typeRef);
		
		assertEquals(2, connections.size());
		assertTrue(connections.keySet().contains("test"));
		assertTrue(connections.keySet().contains("foodmart"));
		
	}

	@Test
	public void testGetSchema() throws Exception {
		
		HttpGet getRequest = new HttpGet("http://localhost:" + port + "/getSchema");
		getRequest.addHeader("accept", "application/xml");
		HttpResponse response = httpClient.execute(getRequest);
		assertEquals(404, response.getStatusLine().getStatusCode());
		
		getRequest = new HttpGet("http://localhost:" + port + "/getSchema?connectionName=foobar");
		getRequest.addHeader("accept", "application/xml");
		response = httpClient.execute(getRequest);
		assertEquals(404, response.getStatusLine().getStatusCode());
		
		getRequest = new HttpGet("http://localhost:" + port + "/getSchema?connectionName=test");
		getRequest.addHeader("accept", "application/xml");
		response = httpClient.execute(getRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		String content = getBodyContent(response);
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document schemaXml = db.parse(new InputSource(new StringReader(content)));
		
		XPath xPath = XPathFactory.newInstance().newXPath();
		Element e = (Element) xPath.evaluate("/Schema[@name='Test']/PhysicalSchema/Table[@name='F1']", schemaXml.getDocumentElement(), XPathConstants.NODE);
		assertNotNull(e);
		
	}
	
	@Test
	public void testQuery() throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();

		HttpPost postRequest = new HttpPost("http://localhost:" + port + "/query");
		StringEntity requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test");
		postRequest.setEntity(requestEntity);
		HttpResponse response = httpClient.execute(postRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		String content = getBodyContent(response);
		CellSetWrapper cellSet = mapper.readValue(content, CellSetWrapper.class);
		assertEquals(1, cellSet.getCellWrappers().size());
		assertEquals(3.0, cellSet.getCellWrappers().get(0).getValue());
		
		requestEntity = buildQueryRequestEntity("test", "bad query");
		postRequest.setEntity(requestEntity);
		response = httpClient.execute(postRequest);
		assertEquals(500, response.getStatusLine().getStatusCode());
		content = getBodyContent(response);
		
		TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};
		Map<String, String> errorMap = mapper.readValue(content, typeRef);
		assertTrue(errorMap.get("reason").matches(".+while parsing.+"));
		assertTrue(errorMap.get("rootCauseReason").matches(".+Syntax.+token.+bad.+"));
		
		requestEntity = buildQueryRequestEntity("test", "select {[DimNotExist].[F1_M1]} on columns from Test");
		postRequest.setEntity(requestEntity);
		response = httpClient.execute(postRequest);
		assertEquals(500, response.getStatusLine().getStatusCode());
		content = getBodyContent(response);
		
		errorMap = mapper.readValue(content, typeRef);
		assertTrue(errorMap.get("reason").matches(".+while parsing.+"));
		assertTrue(errorMap.get("rootCauseReason").matches(".+MDX.+DimNotExist.+not found in cube.+"));
		
	}
	
	@Test
	public void testCachedQueries() throws Exception {
		
		HttpGet flushRequest = new HttpGet("http://localhost:" + port + "/flushCache");
		HttpResponse response = httpClient.execute(flushRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		HttpPost queryRequest = new HttpPost("http://localhost:" + port + "/query");
		StringEntity requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test", true);
		queryRequest.setEntity(requestEntity);
		response = httpClient.execute(queryRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		assertNull(response.getFirstHeader("mondrian-rest-cached-result"));
		getBodyContent(response);
		
		response = httpClient.execute(queryRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("true", response.getFirstHeader("mondrian-rest-cached-result").getValue());
		getBodyContent(response);
		
		flushRequest = new HttpGet("http://localhost:" + port + "/flushCache");
		response = httpClient.execute(flushRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		response = httpClient.execute(queryRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		assertNull(response.getFirstHeader("mondrian-rest-cached-result"));
		
	}

	@Test
	public void testTidy() throws IOException, ClientProtocolException {
		
		ObjectMapper mapper = new ObjectMapper();
		
		HttpPost postRequest = new HttpPost("http://localhost:" + port + "/query");
		StringEntity requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test", true);
		postRequest.setEntity(requestEntity);
		HttpResponse response = httpClient.execute(postRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		String content = getBodyContent(response);
		
		TypeReference<TidyCellSet> typeRef = new TypeReference<TidyCellSet>() {};
		TidyCellSet tidyCellSet = mapper.readValue(content, typeRef);
		List<Map<String, Object>> rows = tidyCellSet.getValues();
		assertEquals(1, rows.size());
		Map<String, Object> row1 = rows.get(0);
		assertEquals(3.0, row1.get("F1_M1"));
		
	}
	
	private StringEntity buildQueryRequestEntity(String connectionName, String queryString) {
		return new StringEntity("{ \"connectionName\" : \"" + connectionName + "\", \"query\" : \"" + queryString + "\"}", ContentType.APPLICATION_JSON);
	}

	private StringEntity buildQueryRequestEntity(String connectionName, String queryString, boolean tidy) {
		return new StringEntity("{ \"connectionName\" : \"" + connectionName + "\", \"query\" : \"" + queryString + "\", \"tidy\" : { \"enabled\": " +
				tidy + "}}", ContentType.APPLICATION_JSON);
	}

	private String getBodyContent(HttpResponse response) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		
		StringBuffer contentBuffer = new StringBuffer();
		String output = null;
		
		while ((output = br.readLine()) != null) {
			contentBuffer.append(output);
		}
		
		return contentBuffer.toString();
		
	}
	
}
