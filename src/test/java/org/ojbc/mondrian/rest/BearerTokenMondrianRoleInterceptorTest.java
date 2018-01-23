package org.ojbc.mondrian.rest;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class BearerTokenMondrianRoleInterceptorTest {
	
	private BearerTokenMondrianRoleInterceptor bearerTokenAdminMondrianRoleInterceptor;
	Map<String, List<String>> tokenRoleMappings = new HashMap<>();
	
	@Before
	public void setUp() {
		tokenRoleMappings.put("TOKEN1", Arrays.asList(new String[] {"ROLE1", "ROLE2"}));
		bearerTokenAdminMondrianRoleInterceptor = new BearerTokenMondrianRoleInterceptor();
		bearerTokenAdminMondrianRoleInterceptor.setTokenRoleMappings(tokenRoleMappings);
		assertNotNull(bearerTokenAdminMondrianRoleInterceptor);
	}
	
	@Test
	public void test() {
		
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer TOKEN1");
		Mockito.when(request.getAttribute(Application.ROLE_REQUEST_ATTRIBUTE_NAME)).thenReturn(tokenRoleMappings.get("TOKEN1"));
		
		boolean success = bearerTokenAdminMondrianRoleInterceptor.authenticateRequest(request, null);
		assertTrue(success);
		@SuppressWarnings("unchecked")
		List<String> roles = (List<String>) request.getAttribute(Application.ROLE_REQUEST_ATTRIBUTE_NAME);
		assertNotNull(roles);
		assertEquals(2, roles.size());
		assertTrue(roles.contains("ROLE1"));
		assertTrue(roles.contains("ROLE2"));
		
	}
	
}
