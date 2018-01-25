package org.ojbc.mondrian.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class BearerTokenRequestAuthorizerTest {
	
	private BearerTokenRequestAuthorizer bearerTokenRequestAuthorizer;
	Map<String, String> tokenRoleMappings = new HashMap<>();
	
	@Before
	public void setUp() {
		tokenRoleMappings.put("TOKEN1", "ROLE1");
		bearerTokenRequestAuthorizer = new BearerTokenRequestAuthorizer();
		bearerTokenRequestAuthorizer.setTokenRoleMappings(tokenRoleMappings);
		assertNotNull(bearerTokenRequestAuthorizer);
	}
	
	@Test
	public void test() {
		
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer TOKEN1");
		
		RequestAuthorizer.RequestAuthorizationStatus status = bearerTokenRequestAuthorizer.authorizeRequest(request, null);
		assertTrue(status.authorized);
		String role = status.mondrianRole;
		assertNotNull(role);
		assertEquals("ROLE1", role);
		
	}
	
}
