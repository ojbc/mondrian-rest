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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ojbc.mondrian.CellSetWrapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "requestAuthorizerBeanName=requestAuthorizerTestAuthorizer" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RequestAuthorizerTest extends AbstractMondrianRestControllerTest {
	
	@LocalServerPort
	private String port;
	
	@Test
	public void testUnauthenticatedAccess() throws Exception {
		HttpEntity<String> requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test");
		ResponseEntity<CellSetWrapper> response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, CellSetWrapper.class);
		// note: no user header...
		assertEquals(403, response.getStatusCode().value());
	}
	
	@Test
	public void testUnlimitedAccess() throws Exception {
		
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put(TestRequestAuthorizer.TEST_REQUEST_AUTHORIZER_USER_HEADER_NAME, TestRequestAuthorizer.UNLIMITED_USER_HEADER_VALUE);
		
		HttpEntity<String> requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F2_M1]} on columns from Test_F2", headerMap);
		
		ResponseEntity<CellSetWrapper> response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, CellSetWrapper.class);
		assertEquals(200, response.getStatusCode().value());
		CellSetWrapper csw = response.getBody();
		assertEquals(1, csw.getCellWrappers().size());
		
		requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F2_M1]} on columns from Test_F2_Secure", headerMap);
		response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, CellSetWrapper.class);
		assertEquals(200, response.getStatusCode().value());
		csw = response.getBody();
		assertEquals(1, csw.getCellWrappers().size());
		
		requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test_F1", headerMap);
		response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, CellSetWrapper.class);
		assertEquals(200, response.getStatusCode().value());
		csw = response.getBody();
		assertEquals(1, csw.getCellWrappers().size());
		
		requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test_F1_Secure", headerMap);
		response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, CellSetWrapper.class);
		assertEquals(200, response.getStatusCode().value());
		csw = response.getBody();
		assertEquals(1, csw.getCellWrappers().size());
		
	}
	
	@Test
	public void testRestrictedAccess() throws Exception {
		
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put(TestRequestAuthorizer.TEST_REQUEST_AUTHORIZER_USER_HEADER_NAME, TestRequestAuthorizer.RESTRICTED_USER_HEADER_VALUE);
		
		HttpEntity<String> requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test_F1", headerMap);
		
		ResponseEntity<CellSetWrapper> response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, CellSetWrapper.class);
		assertEquals(200, response.getStatusCode().value());
		CellSetWrapper csw = response.getBody();
		assertEquals(1, csw.getCellWrappers().size());
		
		requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test_F1_Secure", headerMap);
		response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, CellSetWrapper.class);
		assertEquals(200, response.getStatusCode().value());
		csw = response.getBody();
		assertEquals(1, csw.getCellWrappers().size());
		
		requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F2_M1]} on columns from Test_F2", headerMap);
		response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, CellSetWrapper.class);
		assertEquals(200, response.getStatusCode().value());
		csw = response.getBody();
		assertEquals(1, csw.getCellWrappers().size());
		
		ParameterizedTypeReference<Map<String, String>> responseType = new ParameterizedTypeReference<Map<String, String>>() {};
		
		requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F2_M1]} on columns from Test_F1_Secure", headerMap);
		ResponseEntity<Map<String, String>> errorResponse = restTemplate.exchange(new URI("http://localhost:" + port + "/query"), HttpMethod.POST, requestEntity, responseType);
		assertEquals(500, errorResponse.getStatusCode().value());
		
		Map<String, String> errorMap = errorResponse.getBody();
		
		String rootCauseReason = errorMap.get("rootCauseReason");
		assertTrue(rootCauseReason.matches(".+F2_M1.+not found in cube.+Test_F1_Secure.+"));
		
	}
	
}
