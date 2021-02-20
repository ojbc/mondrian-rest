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
 * Copyright 2012-2018 Open Justice Broker Consortium and Cascadia Analytics LLC
 */
package org.ojbc.mondrian.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.ojbc.mondrian.CellSetWrapper;
import org.ojbc.mondrian.CubeWrapper;
import org.ojbc.mondrian.DimensionWrapper;
import org.ojbc.mondrian.HierarchyWrapper;
import org.ojbc.mondrian.MeasureWrapper;
import org.ojbc.mondrian.MondrianConnectionFactory.MondrianConnection;
import org.ojbc.mondrian.SchemaWrapper;
import org.ojbc.mondrian.TidyCellSetWrapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MondrianRestControllerTest extends AbstractMondrianRestControllerTest {
	
	private final Log log = LogFactory.getLog(MondrianRestControllerTest.class);
	
	@LocalServerPort
	private String port;
	
	@Test
	public void testGetConnections() throws Exception {
		
		ParameterizedTypeReference<Map<String, MondrianConnection>> responseType = new ParameterizedTypeReference<Map<String, MondrianConnection>>() {};
		RequestEntity<Void> request = RequestEntity.get(new URI("http://localhost:" + port + "/getConnections")).build();
		
		ResponseEntity<Map<String, MondrianConnection>> response = restTemplate.exchange(request, responseType);
		Map<String, MondrianConnection> connections = response.getBody();
		
		assertEquals(2, connections.size());
		assertTrue(connections.keySet().contains("test"));
		assertTrue(connections.keySet().contains("foodmart"));
		
	}

	@Test
	public void testGetSchema() throws Exception {
		
		ResponseEntity<String> response = restTemplate.getForEntity(new URI("http://localhost:" + port + "/getSchema"), String.class);
		assertEquals(404, response.getStatusCode().value());
		
		response = restTemplate.getForEntity(new URI("http://localhost:" + port + "/getSchema?connectionName=foobar"), String.class);
		assertEquals(404, response.getStatusCode().value());
		
		response = restTemplate.getForEntity(new URI("http://localhost:" + port + "/getSchema?connectionName=test"), String.class);
		assertEquals(200, response.getStatusCode().value());
		
		String content = response.getBody();
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document schemaXml = db.parse(new InputSource(new StringReader(content)));
		
		XPath xPath = XPathFactory.newInstance().newXPath();
		Element e = (Element) xPath.evaluate("/Schema[@name='Test']/Cube[@name='Test_F1']", schemaXml.getDocumentElement(), XPathConstants.NODE);
		assertNotNull(e);
		
	}
	
	@Test
	public void testGetMetadata() throws Exception {
		
		ResponseEntity<SchemaWrapper> response = restTemplate.getForEntity(new URI("http://localhost:" + port + "/getMetadata"), SchemaWrapper.class);
		assertEquals(404, response.getStatusCode().value());
		
		response = restTemplate.getForEntity(new URI("http://localhost:" + port + "/getMetadata?connectionName=foobar"), SchemaWrapper.class);
		assertEquals(404, response.getStatusCode().value());

		response = restTemplate.getForEntity(new URI("http://localhost:" + port + "/getMetadata?connectionName=test"), SchemaWrapper.class);
		assertEquals(200, response.getStatusCode().value());
		
		SchemaWrapper schemaWrapper = response.getBody();
		assertEquals("Test", schemaWrapper.getName());
		assertEquals(6, schemaWrapper.getCubes().size());
		
		CubeWrapper cubeWrapper = null;
		
		for (CubeWrapper c : schemaWrapper.getCubes()) {
			if (c.getName().equals("Test_F1")) {
				cubeWrapper = c;
			}
		}
		
		assertNotNull(cubeWrapper);
		assertEquals("Test_F1", cubeWrapper.getCaption());
		
		assertEquals(2, cubeWrapper.getMeasures().size());
		
		MeasureWrapper measureWrapper = null;
		MeasureWrapper factCountMeasure = null;
		
		for (MeasureWrapper m : cubeWrapper.getMeasures()) {
			if (m.getName().equals("F1_M1")) {
				measureWrapper = m;
			} else if (m.getName().equals("Fact Count")) {
				factCountMeasure = m;
			}
		}
		
		assertNotNull(measureWrapper);
		assertNotNull(factCountMeasure);
		
		assertEquals("F1_M1", measureWrapper.getName());
		assertEquals("F1_M1", measureWrapper.getCaption());
		assertTrue(measureWrapper.isVisible());
		assertTrue(!measureWrapper.isCalculated());
		
		assertFalse(factCountMeasure.isVisible());
		
		for (DimensionWrapper dw : cubeWrapper.getDimensions()) {
			log.info(dw.getName());
		}
		
		assertEquals(2, cubeWrapper.getDimensions().size());
		
		assertEquals("Measures", cubeWrapper.getDimensions().get(0).getName());
		assertEquals("D1", cubeWrapper.getDimensions().get(1).getName());

		assertEquals(1, schemaWrapper.getCubes().get(0).getDimensions().get(1).getHierarchies().size());
		HierarchyWrapper hw = schemaWrapper.getCubes().get(0).getDimensions().get(1).getHierarchies().get(0);
		assertEquals("D1.D1", hw.getName());
		assertEquals(2, hw.getLevels().size());
		
		assertEquals("D1_DESCRIPTION", hw.getLevels().get(1).getName());
		assertEquals(5, hw.getLevels().get(1).getCardinality());
		
	}
	
	@Test
	public void testCubeSort() throws Exception {
		
		ResponseEntity<SchemaWrapper> response = restTemplate.getForEntity(new URI("http://localhost:" + port + "/getMetadata?connectionName=test"), SchemaWrapper.class);
		SchemaWrapper schemaWrapper = response.getBody();
		
		assertEquals("Test_F1", schemaWrapper.getCubes().get(0).getName());
		assertEquals("Test_F3", schemaWrapper.getCubes().get(2).getName());
		assertEquals("Test_F1_Secure", schemaWrapper.getCubes().get(3).getName());
		assertEquals("Test_F3_Secure", schemaWrapper.getCubes().get(5).getName());
		
	}
	
	@Test
	public void testCachedMetadata() throws Exception {
		
		ResponseEntity<SchemaWrapper> response = restTemplate.getForEntity(new URI("http://localhost:" + port + "/getMetadata?connectionName=test"), SchemaWrapper.class);
		List<String> responseHeaders = response.getHeaders().get("mondrian-rest-cached-result");
		assertNull(responseHeaders);

		response = restTemplate.getForEntity(new URI("http://localhost:" + port + "/getMetadata?connectionName=test"), SchemaWrapper.class);
		responseHeaders = response.getHeaders().get("mondrian-rest-cached-result");
		assertNotNull(responseHeaders);
		responseHeaders = response.getHeaders().get("mondrian-rest-cached-result");
		assertEquals(1, responseHeaders.size());
		assertEquals("true", responseHeaders.get(0));

	}
	
	@Test
	public void testQuery() throws Exception {
		
		HttpEntity<String> requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test_F1");
		ResponseEntity<CellSetWrapper> response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, CellSetWrapper.class);
		assertEquals(200, response.getStatusCode().value());
		
		CellSetWrapper cellSet = response.getBody();
		assertEquals(1, cellSet.getCellWrappers().size());
		assertEquals(3.0, cellSet.getCellWrappers().get(0).getValue());
		
		ParameterizedTypeReference<Map<String, String>> responseType = new ParameterizedTypeReference<Map<String, String>>() {};
		requestEntity = buildQueryRequestEntity("test", "bad query");
		
		ResponseEntity<Map<String, String>> errorResponse = restTemplate.exchange(new URI("http://localhost:" + port + "/query"), HttpMethod.POST, requestEntity, responseType);
		assertEquals(500, errorResponse.getStatusCode().value());
				
		Map<String, String> errorMap = errorResponse.getBody();
		assertTrue(errorMap.get("reason").matches(".+while parsing.+"));
		assertTrue(errorMap.get("rootCauseReason").matches(".+Syntax.+token.+bad.+"));
		
		requestEntity = buildQueryRequestEntity("test", "select {[DimNotExist].[F1_M1]} on columns from Test_F1");
		errorResponse = restTemplate.exchange(new URI("http://localhost:" + port + "/query"), HttpMethod.POST, requestEntity, responseType);
		assertEquals(500, errorResponse.getStatusCode().value());
		
		errorMap = errorResponse.getBody();
		assertTrue(errorMap.get("reason").matches(".+while parsing.+"));
		assertTrue(errorMap.get("rootCauseReason").matches(".+MDX.+DimNotExist.+not found in cube.+"));
		
	}
	
	@Test
	public void testCachedQueries() throws Exception {
		
		HttpEntity<String> requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test_F1");
		ResponseEntity<CellSetWrapper> response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, CellSetWrapper.class);
		assertEquals(200, response.getStatusCode().value());
		List<String> responseHeaders = response.getHeaders().get("mondrian-rest-cached-result");
		assertNull(responseHeaders);
		
		response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, CellSetWrapper.class);
		responseHeaders = response.getHeaders().get("mondrian-rest-cached-result");
		assertEquals(1, responseHeaders.size());
		assertEquals("true", responseHeaders.get(0));
		
		ResponseEntity<Void> flushResponse = restTemplate.getForEntity("http://localhost:" + port + "/flushCache", Void.class);
		assertEquals(200, flushResponse.getStatusCode().value());
		
		response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, CellSetWrapper.class);
		assertEquals(200, response.getStatusCode().value());
		responseHeaders = response.getHeaders().get("mondrian-rest-cached-result");
		assertNull(responseHeaders);
		
	}

	@Test
	public void testTidy() throws Exception {
		
		HttpEntity<String> requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test_F1", true);
		ResponseEntity<TidyCellSetWrapper> response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, TidyCellSetWrapper.class);
		assertEquals(200, response.getStatusCode().value());
		
		TidyCellSetWrapper tidyCellSet = response.getBody();
		List<Map<String, Object>> rows = tidyCellSet.getValues();
		assertEquals(1, rows.size());
		Map<String, Object> row1 = rows.get(0);
		assertEquals(3.0, row1.get("F1_M1"));
		
	}
	
}
