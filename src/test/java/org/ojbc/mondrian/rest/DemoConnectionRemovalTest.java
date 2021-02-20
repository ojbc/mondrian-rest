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

import java.net.URI;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.ojbc.mondrian.MondrianConnectionFactory.MondrianConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "removeDemoConnections=true" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoConnectionRemovalTest {
	
private final Log log = LogFactory.getLog(DemoConnectionRemovalTest.class);
	
	@LocalServerPort
	private String port;
	
	@Autowired
    private TestRestTemplate restTemplate;
	
	@Test
	public void testGetConnections() throws Exception {
		
		ParameterizedTypeReference<Map<String, MondrianConnection>> responseType = new ParameterizedTypeReference<Map<String, MondrianConnection>>() {};
		RequestEntity<Void> request = RequestEntity.get(new URI("http://localhost:" + port + "/getConnections")).build();
		
		ResponseEntity<Map<String, MondrianConnection>> response = restTemplate.exchange(request, responseType);
		Map<String, MondrianConnection> connections = response.getBody();
		
		assertEquals(0, connections.size());
		
	}

}
