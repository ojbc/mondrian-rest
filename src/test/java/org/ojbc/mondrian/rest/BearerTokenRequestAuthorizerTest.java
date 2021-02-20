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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BearerTokenRequestAuthorizerTest {
	
	private BearerTokenRequestAuthorizer bearerTokenRequestAuthorizer;
	Map<String, Map<String, String>> tokenRoleMappings = new HashMap<>();
	
	@BeforeEach
	public void setUp() {
		Map<String, String> innerMap = new HashMap<>();
		innerMap.put("test", "ROLE1");
		tokenRoleMappings.put("TOKEN1", innerMap);
		bearerTokenRequestAuthorizer = new BearerTokenRequestAuthorizer();
		bearerTokenRequestAuthorizer.setTokenRoleMappings(tokenRoleMappings);
		assertNotNull(bearerTokenRequestAuthorizer);
	}
	
	@Test
	public void test() {
		
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer TOKEN1");
		QueryRequest queryRequest = Mockito.mock(QueryRequest.class);
		Mockito.when(queryRequest.getConnectionName()).thenReturn("test");
		
		RequestAuthorizer.RequestAuthorizationStatus status = bearerTokenRequestAuthorizer.authorizeRequest(request, queryRequest.getConnectionName());
		assertTrue(status.authorized);
		String role = status.mondrianRole;
		assertNotNull(role);
		assertEquals("ROLE1", role);
		
	}
	
}
