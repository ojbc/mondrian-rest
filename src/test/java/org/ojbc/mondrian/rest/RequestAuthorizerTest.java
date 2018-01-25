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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ojbc.mondrian.CellSetWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = { "requestAuthorizerBeanName=requestAuthorizerTestAuthorizer" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RequestAuthorizerTest extends AbstractMondrianRestControllerTest {
	
	private final Log log = LogFactory.getLog(RequestAuthorizerTest.class);
	
	@Autowired
	private MondrianRestController controller;

	@LocalServerPort
	private String port;
	
	private HttpClient httpClient;

	@Before
    public void setUp() throws Exception {
    	RequestConfig requestConfig = RequestConfig.custom().build();
    	HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    	clientBuilder.setDefaultRequestConfig(requestConfig);
    	httpClient = clientBuilder.build();
    	assertNotNull(httpClient);
    	assertNotNull(controller);
    	log.info("Randomly-assigned port is " + port);
    }
	
	@Test
	public void testUnauthenticatedAccess() throws Exception {
		HttpPost postRequest = new HttpPost("http://localhost:" + port + "/query");
		StringEntity requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F2_M1]} on columns from Test");
		postRequest.setEntity(requestEntity);
		// note: no user header...
		HttpResponse response = httpClient.execute(postRequest);
		assertEquals(403, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testUnlimitedAccess() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		HttpPost postRequest = new HttpPost("http://localhost:" + port + "/query");
		StringEntity requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F2_M1]} on columns from Test");
		postRequest.setEntity(requestEntity);
		postRequest.setHeader(TestRequestAuthorizer.TEST_REQUEST_AUTHORIZER_USER_HEADER_NAME, TestRequestAuthorizer.UNLIMITED_USER_HEADER_VALUE);
		HttpResponse response = httpClient.execute(postRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		String content = getBodyContent(response);
		CellSetWrapper csw = mapper.readValue(content, CellSetWrapper.class);
		assertEquals(1, csw.getCellWrappers().size());
		requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F2_M1]} on columns from Test_Secure");
		postRequest.setEntity(requestEntity);
		postRequest.setHeader(TestRequestAuthorizer.TEST_REQUEST_AUTHORIZER_USER_HEADER_NAME, TestRequestAuthorizer.UNLIMITED_USER_HEADER_VALUE);
		response = httpClient.execute(postRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		content = getBodyContent(response);
		csw = mapper.readValue(content, CellSetWrapper.class);
		assertEquals(1, csw.getCellWrappers().size());
		requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test");
		postRequest.setEntity(requestEntity);
		postRequest.setHeader(TestRequestAuthorizer.TEST_REQUEST_AUTHORIZER_USER_HEADER_NAME, TestRequestAuthorizer.UNLIMITED_USER_HEADER_VALUE);
		response = httpClient.execute(postRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		content = getBodyContent(response);
		csw = mapper.readValue(content, CellSetWrapper.class);
		assertEquals(1, csw.getCellWrappers().size());
		requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test_Secure");
		postRequest.setEntity(requestEntity);
		postRequest.setHeader(TestRequestAuthorizer.TEST_REQUEST_AUTHORIZER_USER_HEADER_NAME, TestRequestAuthorizer.UNLIMITED_USER_HEADER_VALUE);
		response = httpClient.execute(postRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		content = getBodyContent(response);
		csw = mapper.readValue(content, CellSetWrapper.class);
		assertEquals(1, csw.getCellWrappers().size());
	}
	
	@Test
	public void testRestrictedAccess() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		HttpPost postRequest = new HttpPost("http://localhost:" + port + "/query");
		StringEntity requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test");
		postRequest.setEntity(requestEntity);
		postRequest.setHeader(TestRequestAuthorizer.TEST_REQUEST_AUTHORIZER_USER_HEADER_NAME, TestRequestAuthorizer.RESTRICTED_USER_HEADER_VALUE);
		HttpResponse response = httpClient.execute(postRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		String content = getBodyContent(response);
		CellSetWrapper csw = mapper.readValue(content, CellSetWrapper.class);
		assertEquals(1, csw.getCellWrappers().size());
		requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F1_M1]} on columns from Test_Secure");
		postRequest.setEntity(requestEntity);
		postRequest.setHeader(TestRequestAuthorizer.TEST_REQUEST_AUTHORIZER_USER_HEADER_NAME, TestRequestAuthorizer.RESTRICTED_USER_HEADER_VALUE);
		response = httpClient.execute(postRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		content = getBodyContent(response);
		csw = mapper.readValue(content, CellSetWrapper.class);
		assertEquals(1, csw.getCellWrappers().size());
		requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F2_M1]} on columns from Test");
		postRequest.setEntity(requestEntity);
		postRequest.setHeader(TestRequestAuthorizer.TEST_REQUEST_AUTHORIZER_USER_HEADER_NAME, TestRequestAuthorizer.RESTRICTED_USER_HEADER_VALUE);
		response = httpClient.execute(postRequest);
		assertEquals(200, response.getStatusLine().getStatusCode());
		content = getBodyContent(response);
		csw = mapper.readValue(content, CellSetWrapper.class);
		assertEquals(1, csw.getCellWrappers().size());
		requestEntity = buildQueryRequestEntity("test", "select {[Measures].[F2_M1]} on columns from Test_Secure");
		postRequest.setEntity(requestEntity);
		postRequest.setHeader(TestRequestAuthorizer.TEST_REQUEST_AUTHORIZER_USER_HEADER_NAME, TestRequestAuthorizer.RESTRICTED_USER_HEADER_VALUE);
		response = httpClient.execute(postRequest);
		assertEquals(500, response.getStatusLine().getStatusCode());
		content = getBodyContent(response);
		Map<String, String> errorResponse = getContentAsMap(content);
		String rootCauseReason = errorResponse.get("rootCauseReason");
		assertTrue(rootCauseReason.matches(".+F2_M1.+not found in cube.+Test_Secure.+"));
	}
	
}
